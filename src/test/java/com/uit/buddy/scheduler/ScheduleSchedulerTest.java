package com.uit.buddy.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.entity.learning.TemporaryDeadline;
import com.uit.buddy.entity.redis.Deadline;
import com.uit.buddy.enums.DeadlineStatus;
import com.uit.buddy.repository.learning.DeadlineRepository;
import com.uit.buddy.repository.learning.TemporaryDeadlineRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.service.learning.AssignmentService;
import com.uit.buddy.service.notification.NotificationService;

@ExtendWith(MockitoExtension.class)
class ScheduleSchedulerTest {

    @Mock
    private ScheduleService scheduleService;
    @Mock
    private AssignmentService assignmentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private DeadlineRepository deadlineRepository;
    @Mock
    private TemporaryDeadlineRepository temporaryDeadlineRepository;

    private ScheduleScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ScheduleScheduler(
                scheduleService,
                assignmentService,
                notificationService,
                studentRepository,
                deadlineRepository,
                temporaryDeadlineRepository);
        ScheduleScheduler.stop = false;
        ScheduleScheduler.isRunning = true;
    }

    @Test
    void zeroStudents_shouldNotFetchAnyDeadline() {
        when(studentRepository.findMssvAll()).thenReturn(List.of());

        scheduler.scrapeAllDeadlineOfStudent();

        verify(scheduleService, never()).fetchCourseDeadlinesFromMoodle(any(), any(), any());
        verify(assignmentService, never()).getDeadlineWithMssv(any(), any(), any());
        verify(deadlineRepository, never()).save(any());
    }

    @Test
    void oneStudent_upcomingWithin10Minutes_shouldSaveToRedis() {
        String mssv = "24521784";
        when(studentRepository.findMssvAll()).thenReturn(List.of(mssv));
        when(scheduleService.fetchCourseDeadlinesFromMoodle(eq(mssv), any(), any()))
                .thenReturn(List.of(course("CSE101",
                        exercise("Lab 1", LocalDateTime.now().plusMinutes(5), DeadlineStatus.UPCOMING))));
        when(assignmentService.getDeadlineWithMssv(eq(mssv), any(), any())).thenReturn(List.of());

        scheduler.scrapeAllDeadlineOfStudent();

        verify(deadlineRepository, times(1)).save(any(Deadline.class));
    }

    @Test
    void manyStudents_shouldFetchDeadlinesForEachStudent() {
        List<String> students = List.of("24521784", "24521785", "24521786");
        when(studentRepository.findMssvAll()).thenReturn(students);
        when(scheduleService.fetchCourseDeadlinesFromMoodle(any(), any(), any())).thenReturn(List.of());
        when(assignmentService.getDeadlineWithMssv(any(), any(), any())).thenReturn(List.of());

        scheduler.scrapeAllDeadlineOfStudent();

        verify(scheduleService, times(3)).fetchCourseDeadlinesFromMoodle(any(), any(), any());
        verify(assignmentService, times(3)).getDeadlineWithMssv(any(), any(), any());
    }

    @Test
    void boundaryNearDeadlineTriggerUnder30Seconds_shouldPushNearNotification() {
        Deadline redisDeadline = Deadline.builder()
                .mssv_deadline("id-1")
                .mssv("24521784")
                .deadlineName("Midterm")
                .dueDate(LocalDateTime.now().plusHours(24).plusSeconds(20).toLocalDate())
                .dueTime(LocalDateTime.now().plusHours(24).plusSeconds(20).toLocalTime())
                .build();
        when(deadlineRepository.findAll()).thenReturn(List.of(redisDeadline));

        scheduler.pushNotiForDeadline();

        verify(notificationService, times(1)).createNearDeadlineNotification("24521784", "Midterm");
    }

    @Test
    void overdueDeadlineCrossedThreshold_shouldDeleteFromRedis() {
        Deadline overdueDeadline = Deadline.builder()
                .mssv_deadline("id-overdue")
                .mssv("24521784")
                .deadlineName("Lab 1")
                .dueDate(LocalDateTime.now().minusSeconds(20).toLocalDate())
                .dueTime(LocalDateTime.now().minusSeconds(20).toLocalTime())
                .build();
        when(deadlineRepository.findAll()).thenReturn(List.of(overdueDeadline));

        scheduler.pushNotiForDeadline();

        verify(notificationService).createOverdueDeadlineNotification("24521784", "Lab 1");
        verify(deadlineRepository).deleteById("id-overdue");
    }

    @Test
    void pingMoodleAndPushNewDeadlines_shouldCreateNotificationForEachNewDeadline() {
        String mssv = "24521784";
        TemporaryDeadline td1 = TemporaryDeadline.builder()
                .mssv(mssv)
                .classCode("CSE101")
                .deadlineName("Quiz 1")
                .dueDate(LocalDateTime.now().plusDays(1))
                .build();
        TemporaryDeadline td2 = TemporaryDeadline.builder()
                .mssv(mssv)
                .classCode("CSE102")
                .deadlineName("Assignment 1")
                .dueDate(LocalDateTime.now().plusDays(2))
                .build();
        when(studentRepository.findMssvAll()).thenReturn(List.of(mssv));
        when(scheduleService.getUpcomingDeadlines(mssv)).thenReturn(List.of(td1, td2));

        scheduler.pingMoodleAndPushNewDeadlines();

        verify(notificationService).createNewDeadlineNotification(mssv, "Quiz 1");
        verify(notificationService).createNewDeadlineNotification(mssv, "Assignment 1");
    }

    @Test
    void pingMoodleAndPushNewDeadlines_noNewDeadlines_shouldNotCreateAnyNotification() {
        String mssv = "24521784";
        when(studentRepository.findMssvAll()).thenReturn(List.of(mssv));
        when(scheduleService.getUpcomingDeadlines(mssv)).thenReturn(List.of());

        scheduler.pingMoodleAndPushNewDeadlines();

        verify(notificationService, never()).createNewDeadlineNotification(any(), any());
    }

    @Test
    void exceptionOnFirstStudent_shouldContinueToSecondStudent() {
        String first = "24521784";
        String second = "24521785";
        when(studentRepository.findMssvAll()).thenReturn(List.of(first, second));
        when(scheduleService.fetchCourseDeadlinesFromMoodle(eq(first), any(), any()))
                .thenThrow(new RuntimeException("moodle error"));
        when(scheduleService.fetchCourseDeadlinesFromMoodle(eq(second), any(), any()))
                .thenReturn(List.of(course("CSE101",
                        exercise("Final", LocalDateTime.now().plusMinutes(5), DeadlineStatus.UPCOMING))));
        when(assignmentService.getDeadlineWithMssv(eq(second), any(), any())).thenReturn(List.of());

        scheduler.scrapeAllDeadlineOfStudent();

        verify(scheduleService).fetchCourseDeadlinesFromMoodle(eq(first), any(), any());
        verify(scheduleService).fetchCourseDeadlinesFromMoodle(eq(second), any(), any());
        verify(deadlineRepository, times(1)).save(any(Deadline.class));
    }

    private CourseContentResponse course(String courseName, CourseContentResponse.exercise... exercises) {
        return new CourseContentResponse(courseName, List.of(exercises));
    }

    private CourseContentResponse.exercise exercise(String name, LocalDateTime dueDate, DeadlineStatus status) {
        return new CourseContentResponse.exercise(name, dueDate, null, status, false);
    }
}
