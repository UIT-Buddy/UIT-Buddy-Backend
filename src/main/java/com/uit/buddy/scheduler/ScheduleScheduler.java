package com.uit.buddy.scheduler;

import com.uit.buddy.constant.ScheduleConstant;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.learning.TemporaryDeadline;
import com.uit.buddy.enums.DeadlineStatus;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.learning.TemporaryDeadlineRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.service.learning.AssignmentService;
import com.uit.buddy.service.notification.NotificationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private final TemporaryDeadlineRepository temporaryDeadlineRepository;
    private final SemesterRepository semesterRepository;
    public static Boolean stop = false;
    public static Boolean isRunning = true;

    public ScheduleScheduler(ScheduleService scheduleService, AssignmentService assignmentService,
            NotificationService notificationService, StudentRepository studentRepository,
            TemporaryDeadlineRepository temporaryDeadlineRepository, SemesterRepository semesterRepository) {
        this.scheduleService = scheduleService;
        this.assignmentService = assignmentService;
        this.notificationService = notificationService;
        this.studentRepository = studentRepository;
        this.temporaryDeadlineRepository = temporaryDeadlineRepository;
        this.semesterRepository = semesterRepository;
    }

    /**
     * Signal the scheduler to stop after the current cycle.
     */
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

    /**
     * Global scheduler — runs every 15 minutes. For the active semester, fetches all deadlines from Moodle + student
     * tasks for every enrolled month, checks for new deadlines and pushes notifications, then saves to
     * TemporaryDeadline table.
     */
    @Scheduled(fixedDelay = ScheduleConstant.SCRAPE_DEADLINE_INTERVAL)
    public void scrapeAllDeadlineOfStudent() {
        Semester semester = semesterRepository.findCurrentSemester(LocalDate.now()).orElse(null);
        if (semester == null) {
            log.warn("[SCHEDULER] No active semester found, skipping deadline scrape");
            return;
        }

        List<MonthYear> semesterMonths = getSemesterMonthYears(semester);
        if (semesterMonths.isEmpty()) {
            log.warn("[SCHEDULER] Active semester has no valid month range: {}", semester.getSemesterCode());
            return;
        }

        log.info("[START GLOBAL SCHEDULER] for semester {} ({} months)", semester.getSemesterCode(),
                semesterMonths.size());

        List<String> listMssv = studentRepository.findMssvAll();
        for (String mssv : listMssv) {
            try {
                for (MonthYear my : semesterMonths) {
                    List<CourseContentResponse> moodleDeadlines = scheduleService.fetchCourseDeadlinesFromMoodle(mssv,
                            my.month(), my.year());
                    List<CourseContentResponse> studentTaskDeadlines = assignmentService.getDeadlineWithMssv(mssv,
                            my.month(), my.year());
                    processDeadlines(moodleDeadlines, studentTaskDeadlines, mssv);
                    Thread.sleep(ScheduleConstant.GAP_PER_STUDENT_MONTHLY_FETCH_DEADLINE);
                }
                Thread.sleep(ScheduleConstant.GAP_PER_STUDENT_PING_MOODLE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Scheduler interrupted for mssv={}", mssv);
                break;
            } catch (Exception e) {
                log.error("Failed to fetch deadline for mssv={}", mssv, e);
            }
        }
    }

    /**
     * Returns all (month, year) pairs that fall within the semester's startDate and endDate. Example: Sem1 (Jan–Jun) →
     * (1,2025)..(6,2025); Sem2 (Aug–Jan) → (8,2025)..(1,2026).
     */
    private List<MonthYear> getSemesterMonthYears(Semester semester) {
        List<MonthYear> result = new ArrayList<>();
        LocalDate start = semester.getStartDate();
        LocalDate end = semester.getEndDate();

        if (start == null || end == null) {
            return result;
        }

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            result.add(new MonthYear(cursor.getMonthValue(), cursor.getYear()));
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    private record MonthYear(int month, int year) {
    }

    /**
     * Child scheduler — runs every 30 seconds. Compares due time in TemporaryDeadline to push deadline reminders for
     * two cases: at due time (on-time) and 24 hours before due.
     */
    @Scheduled(fixedDelay = ScheduleConstant.PUSH_NOTIFICATION_INTERVAL)
    public void pushNotiForDeadline() {
        LocalDateTime now = LocalDateTime.now();

        // Case 1: deadlines due within the next 24 hours
        LocalDateTime twentyFourHoursFromNow = now.plusHours(ScheduleConstant.NEAR_DEADLINE_HOURS);
        List<TemporaryDeadline> nearDeadlines = temporaryDeadlineRepository.findDeadlinesDueBetween(now,
                twentyFourHoursFromNow);

        for (TemporaryDeadline deadline : nearDeadlines) {
            if (deadline.getDueDate() == null)
                continue;
            if (isWithinThreshold(now, deadline.getDueDate())) {
                log.info("[PUSH NOTI 24H]: User {} has a deadline due within 24h: {}", deadline.getMssv(),
                        deadline.getDeadlineName());
                notificationService.createNearDeadlineNotification(deadline.getMssv(), deadline.getDeadlineName(), "");
            }
        }

        // Case 2: deadlines at or past their due time (overdue)
        List<TemporaryDeadline> overdueDeadlines = temporaryDeadlineRepository.findOverdueDeadlines(now);
        for (TemporaryDeadline deadline : overdueDeadlines) {
            if (deadline.getDueDate() == null)
                continue;
            if (isWithinThreshold(now, deadline.getDueDate())) {
                log.info("[PUSH NOTI OVERDUE]: User {} has an overdue deadline: {}", deadline.getMssv(),
                        deadline.getDeadlineName());
                notificationService.createOverdueDeadlineNotification(deadline.getMssv(), deadline.getDeadlineName(),
                        "");
            }
        }
    }

    private boolean isWithinThreshold(LocalDateTime now, LocalDateTime targetTime) {
        long secondsUntilTarget = java.time.Duration.between(now, targetTime).getSeconds();
        boolean lessThan30SecBefore = secondsUntilTarget >= 0 && secondsUntilTarget < 30;
        boolean crossedWithin30Sec = secondsUntilTarget < 0 && Math.abs(secondsUntilTarget) < 30;
        return lessThan30SecBefore || crossedWithin30Sec;
    }

    private void processDeadlines(List<CourseContentResponse> moodleDeadlines,
            List<CourseContentResponse> studentTaskDeadlines, String mssv) {
        List<CourseContentResponse> allDeadlines = new ArrayList<>(moodleDeadlines);
        allDeadlines.addAll(studentTaskDeadlines);

        List<CourseContentResponse> allDeadlinesFlat = allDeadlines.stream().filter(c -> c.exercises() != null)
                .flatMap(c -> c.exercises().stream()
                        .filter(e -> isTrackedStatus(e) && e.dueDate() != null && isWithinBuffer(e))
                        .map(e -> new CourseContentResponse(c.courseName(), List.of(e))))
                .toList();

        if (allDeadlinesFlat.isEmpty())
            return;

        for (CourseContentResponse course : allDeadlinesFlat) {
            for (CourseContentResponse.exercise exercise : course.exercises()) {
                syncDeadline(mssv, course.courseName(), exercise);
            }
        }
    }

    private boolean isTrackedStatus(CourseContentResponse.exercise exercise) {
        return exercise.status() == DeadlineStatus.UPCOMING || exercise.status() == DeadlineStatus.NEARDEADLINE;
    }

    private boolean isWithinBuffer(CourseContentResponse.exercise exercise) {
        if (exercise.dueDate() == null)
            return false;
        long minutesUntilDue = java.time.Duration.between(LocalDateTime.now(), exercise.dueDate()).toMinutes();
        if (minutesUntilDue < 0)
            return false;
        if (exercise.status() == DeadlineStatus.NEARDEADLINE) {
            return minutesUntilDue <= ScheduleConstant.NEAR_DEADLINE_BUFFER_MINUTES;
        }
        return minutesUntilDue < ScheduleConstant.OVERDUE_BUFFER_MINUTES;
    }

    private void syncDeadline(String mssv, String courseName, CourseContentResponse.exercise exercise) {
        String classCode = (courseName == null || courseName.isBlank()) ? ScheduleConstant.UNKNOWN_CLASS_CODE
                : courseName;
        String deadlineName = exercise.exerciseName();
        LocalDateTime dueDate = exercise.dueDate();
        if (deadlineName == null || deadlineName.isBlank() || dueDate == null)
            return;

        // Check if already exists
        List<TemporaryDeadline> existingList = temporaryDeadlineRepository.findByMssv(mssv);
        TemporaryDeadline existing = existingList.stream()
                .filter(td -> td.getClassCode().equalsIgnoreCase(classCode)
                        && td.getDeadlineName().equalsIgnoreCase(deadlineName.trim())
                        && td.getDueDate().equals(dueDate))
                .findFirst().orElse(null);

        if (existing == null) {
            TemporaryDeadline td = TemporaryDeadline.builder().mssv(mssv).classCode(classCode)
                    .deadlineName(deadlineName.trim()).dueDate(dueDate).status(exercise.status()).url(exercise.url())
                    .build();
            temporaryDeadlineRepository.save(td);
            log.info("[SCHEDULER] Saved new deadline for mssv={}, classCode={}, deadline={}", mssv, classCode,
                    deadlineName);
        } else {
            if (existing.getStatus() != exercise.status()) {
                existing.setStatus(exercise.status());
            }
            if (!java.util.Objects.equals(existing.getUrl(), exercise.url())) {
                existing.setUrl(exercise.url());
            }

        }
    }
}
