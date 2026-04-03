package com.uit.buddy.scheduler;

import com.uit.buddy.constant.ScheduleConstant;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.entity.learning.TemporaryDeadline;
import com.uit.buddy.entity.redis.Deadline;
import com.uit.buddy.enums.DeadlineStatus;
import com.uit.buddy.repository.learning.DeadlineRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.service.learning.AssignmentService;
import com.uit.buddy.service.notification.NotificationService;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ScheduleScheduler {

    private final ScheduleService scheduleService;
    private final AssignmentService assignmentService;
    private final NotificationService notificationService;
    private final StudentRepository studentRepository;
    private final DeadlineRepository deadlineRepository;
    public static Boolean stop = false;
    public static Boolean isRunning = true;

    // This method can be called from outside to signal the scheduler to stop after the current cycle, avoid the risk of
    // interrupting the schedule in the middle of processing a student
    public static void stopSchedule() {
        if (isRunning)
            stop = true;
    }

    private void refreshSchedule() {
        stop = false;
        isRunning = false;
    }

    private void startSchedule() {
        isRunning = true;
    }

    public ScheduleScheduler(ScheduleService scheduleService, AssignmentService assignmentService,
            NotificationService notificationService, StudentRepository studentRepository,
            DeadlineRepository deadlineRepository) {
        this.scheduleService = scheduleService;
        this.assignmentService = assignmentService;
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.deadlineRepository = deadlineRepository;
    }

    @Scheduled(fixedDelay = ScheduleConstant.SCRAPE_DEADLINE_INTERVAL)
    public void scrapeAllDeadlineOfStudent() {
        LocalDate now = LocalDate.now();
        Integer currentMonth = now.getMonthValue();
        Integer currentYear = now.getYear();

        log.info("[START GLOBAL SCHEDULER]");
        startSchedule();
        List<String> listMssv = studentRepository.findMssvAll();
        for (String s : listMssv) {
            if (stop) {
                log.info("[FETCH DEADLINE DETECTED]: STOP!");
                break;
            }

            String mssv = s;
            try {
                List<CourseContentResponse> moodleDeadlines = scheduleService.fetchCourseDeadlinesFromMoodle(mssv,
                        currentMonth, currentYear);
                List<CourseContentResponse> studentTaskDeadlines = assignmentService.getDeadlineWithMssv(mssv,
                        currentMonth, currentYear);
                List<CourseContentResponse> allDeadlines = new ArrayList<>(moodleDeadlines);
                allDeadlines.addAll(studentTaskDeadlines);

                processDeadline(allDeadlines, mssv);
                Thread.sleep(ScheduleConstant.GAP_PER_STUDENT_PING_MOODLE);
            } catch (Exception e) {
                log.error("Failed to fetch deadline for mssv={}", mssv, e);
            }
        }
        refreshSchedule();
    }

    @Scheduled(fixedDelay = ScheduleConstant.PUSH_NOTIFICATION_INTERVAL)
    public void pushNotiForDeadline() {
        LocalDateTime now = LocalDateTime.now();
        List<Deadline> deadlines = getAllDeadlinesFromRedis();

        for (Deadline deadline : deadlines) {
            if (deadline.getDueDate() == null || deadline.getDueTime() == null) {
                continue;
            }

            LocalDateTime dueDateTime = LocalDateTime.of(deadline.getDueDate(), deadline.getDueTime());
            LocalDateTime nearDeadlineTrigger = dueDateTime.minusHours(ScheduleConstant.NEAR_DEADLINE_HOURS);

            if (shouldPushWhenNearOrCrossedThreshold(now, nearDeadlineTrigger)) {
                log.info("[PUSH NOTI]: User {} has a due soon deadline: {}", deadline.getMssv(),
                        deadline.getDeadlineName());
                notificationService.createNearDeadlineNotification(deadline.getMssv(), deadline.getDeadlineName(), "");
            }

            if (shouldPushWhenNearOrCrossedThreshold(now, dueDateTime)) {
                log.info("[PUSH NOTI]: User {} has an overdue deadline: {}", deadline.getMssv(),
                        deadline.getDeadlineName());
                notificationService.createOverdueDeadlineNotification(deadline.getMssv(), deadline.getDeadlineName(),
                        "");
                deadlineRepository.deleteById(deadline.getMssv_deadline());
            }
        }
    }

    // Runs once daily at 12:00 PM
    @Scheduled(cron = "0 0 12 * * ?")
    public void pingMoodleAndPushNewDeadlines() {
        List<String> listMssv = studentRepository.findMssvAll();

        for (String mssv : listMssv) {
            try {
                List<TemporaryDeadline> newDeadlines = scheduleService.getUpcomingDeadlines(mssv);

                for (TemporaryDeadline deadline : newDeadlines) {
                    notificationService.createNewDeadlineNotification(mssv, deadline.getDeadlineName(), "");
                }

                log.info("[PING MOODLE SCHEDULER] Sync complete for mssv={}, TaskCount={}", mssv, newDeadlines.size());
            } catch (Exception e) {
                log.error("[PING MOODLE SCHEDULER] Failed for mssv={}", mssv, e);
            }
        }
    }

    private List<Deadline> getAllDeadlinesFromRedis() {
        Iterable<Deadline> storedDeadlines = deadlineRepository.findAll();
        List<Deadline> deadlines = new ArrayList<>();
        storedDeadlines.forEach(deadlines::add);
        return deadlines;
    }

    private boolean shouldPushWhenNearOrCrossedThreshold(LocalDateTime now, LocalDateTime targetTime) {
        long secondsUntilTarget = Duration.between(now, targetTime).getSeconds();
        boolean lessThanThirtySecondsBefore = secondsUntilTarget >= 0 && secondsUntilTarget < 30;
        boolean crossedThresholdRecently = secondsUntilTarget < 0 && Math.abs(secondsUntilTarget) < 30;
        return lessThanThirtySecondsBefore || crossedThresholdRecently;
    }

    private void processDeadline(List<CourseContentResponse> courseList, String mssv) {
        if (courseList == null || courseList.isEmpty()) {
            return;
        }

        LocalDateTime baseTime = LocalDateTime.now();
        List<CompletableFuture<Void>> deadlineTasks = courseList.stream()
                .flatMap(
                        course -> course.exercises().stream().filter(this::isTrackedStatus)
                                .filter(exercise -> shouldSaveToRedis(exercise, baseTime))
                                .map(exercise -> CompletableFuture
                                        .runAsync(() -> saveDeadlineIfAbsent(mssv, course.courseName(), exercise))))
                .toList();
        CompletableFuture.allOf(deadlineTasks.toArray(CompletableFuture[]::new)).join();
    }

    private boolean isTrackedStatus(CourseContentResponse.exercise exercise) {
        return exercise.status() == DeadlineStatus.UPCOMING || exercise.status() == DeadlineStatus.NEARDEADLINE;
    }

    private boolean shouldSaveToRedis(CourseContentResponse.exercise exercise, LocalDateTime baseTime) {
        long nearDeadlineBufferMinutes = ScheduleConstant.NEAR_DEADLINE_BUFFER_MINUTES;
        long overdueBufferMinutes = ScheduleConstant.OVERDUE_BUFFER_MINUTES;
        LocalDateTime dueDate = exercise.dueDate();
        if (dueDate == null) {
            return false;
        }

        long minutesUntilDue = Duration.between(baseTime, dueDate).toMinutes();
        if (minutesUntilDue < 0) {
            return false;
        }

        if (exercise.status() == DeadlineStatus.NEARDEADLINE) {
            return minutesUntilDue <= nearDeadlineBufferMinutes;
        }

        return minutesUntilDue < overdueBufferMinutes;
    }

    private void saveDeadlineIfAbsent(String mssv, String courseName, CourseContentResponse.exercise exercise) {
        String deadlineId = buildDeadlineId(mssv, courseName, exercise.exerciseName(), exercise.dueDate());
        if (deadlineRepository.existsById(deadlineId)) {
            return;
        }
        Deadline deadline = Deadline.builder().mssv_deadline(deadlineId).mssv(mssv)
                .deadlineName(exercise.exerciseName()).dueDate(exercise.dueDate().toLocalDate())
                .dueTime(exercise.dueDate().toLocalTime()).build();
        log.info("[SCHEDULER] Push a {} deadline for user with ID {}", exercise.status(), mssv);
        deadlineRepository.save(deadline);
    }

    private String buildDeadlineId(String mssv, String courseName, String exerciseName, LocalDateTime dueDate) {
        String raw = String.join("_", mssv, courseName.trim(), exerciseName.trim(), dueDate.toString());
        return raw.toLowerCase(Locale.ROOT);
    }

}
