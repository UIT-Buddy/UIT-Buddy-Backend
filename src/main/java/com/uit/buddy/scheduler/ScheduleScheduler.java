package com.uit.buddy.scheduler;

import com.uit.buddy.dto.response.schedule.CourseContentResponse;
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

    @Scheduled(fixedDelay = 60000)
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
                Thread.sleep(1000);
            } catch (Exception e) {
                log.error("Failed to fetch deadline for mssv={}", mssv, e);
            }
        }
        refreshSchedule();
    }

    @Scheduled(fixedDelay = 30000)
    public void pushNotiForDeadline() {
        LocalDateTime now = LocalDateTime.now();
        List<Deadline> deadlines = getAllDeadlinesFromRedis();

        for (Deadline deadline : deadlines) {
            if (deadline.getDueDate() == null || deadline.getDueTime() == null) {
                continue;
            }

            LocalDateTime dueDateTime = LocalDateTime.of(deadline.getDueDate(), deadline.getDueTime());
            LocalDateTime nearDeadlineTrigger = dueDateTime.minusHours(24);

            if (shouldPushWhenNearOrCrossedThreshold(now, nearDeadlineTrigger)) {
                log.info("[PUSH NOTI]: User {} has a due soon deadline: {}", deadline.getMssv(),
                        deadline.getDeadlineName());
                notificationService.createNearDeadlineNotification(deadline.getMssv(), deadline.getDeadlineName());
            }

            if (shouldPushWhenNearOrCrossedThreshold(now, dueDateTime)) {
                log.info("[PUSH NOTI]: User {} has an overdue deadline: {}", deadline.getMssv(),
                        deadline.getDeadlineName());
                notificationService.createOverdueDeadlineNotification(deadline.getMssv(), deadline.getDeadlineName());
                deadlineRepository.deleteById(deadline.getMssv_deadline());
            }
        }
    }

    @Scheduled(cron = "0 0 0 ? * MON,WED,FRI,SUN")
    public void pingMoodleAndPushDeadlineSummary() {
        log.info("[SUMMARY SCHEDULER] Start Moodle deadline summary job");
        List<String> listMssv = studentRepository.findMssvAll();

        for (String mssv : listMssv) {
            try {
                List<CourseContentResponse> moodleDeadlines = scheduleService.fetchCourseDeadlinesFromMoodle(mssv, null,
                        null);
                int uncompletedCount = countUncompletedDeadlines(moodleDeadlines);

                notificationService.createDeadlineSummaryNotification(mssv, uncompletedCount);
                log.info("[SUMMARY SCHEDULER] Pushed summary for mssv={}, uncompletedCount={}", mssv, uncompletedCount);
            } catch (Exception e) {
                log.error("[SUMMARY SCHEDULER] Failed for mssv={}", mssv, e);
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

    private int countUncompletedDeadlines(List<CourseContentResponse> courses) {
        if (courses == null || courses.isEmpty()) {
            return 0;
        }

        return courses.stream().flatMap(course -> course.exercises().stream())
                .map(CourseContentResponse.exercise::status).filter(status -> status != DeadlineStatus.DONE)
                .mapToInt(status -> 1).sum();
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
        long nearDeadlineBufferMinutes = 24 * 60; // 24 hours + 10 minutes buffer
        long overdueBufferMinutes = 12;
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
