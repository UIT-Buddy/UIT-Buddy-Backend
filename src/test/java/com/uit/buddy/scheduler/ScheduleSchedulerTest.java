package com.uit.buddy.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.uit.buddy.client.UitClient;
import com.uit.buddy.constant.ScheduleConstant;
import com.uit.buddy.entity.academic.Course;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.redis.IncomingSubject;
import com.uit.buddy.redis.IncomingSubjectRepository;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.academic.StudentSubjectClassRepository;
import com.uit.buddy.repository.learning.TemporaryDeadlineRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.service.encryption.WsTokenEncryptionService;
import com.uit.buddy.service.learning.AssignmentService;
import com.uit.buddy.service.notification.NotificationService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private TemporaryDeadlineRepository temporaryDeadlineRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private WsTokenEncryptionService wsTokenEncryptionService;
    @Mock
    private UitClient uitClient;
    @Mock
    private StudentSubjectClassRepository studentSubjectClassRepository;
    @Mock
    private IncomingSubjectRepository incomingSubjectRepository;

    @InjectMocks
    private ScheduleScheduler scheduleScheduler;

    private String mssv = "22520001";
    private String classCode = "IT001.N11";
    private String subjectName = "Calculas";
    private String roomCode = "C101";

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldSendNotificationWhenClassIsUpcomingAndNotNotified() {
        // Arrange
        LocalTime now = LocalTime.now();
        LocalTime startTime = now.plusMinutes(25);
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue() + 1;

        Course course = Course.builder().courseName(subjectName).build();
        SubjectClass sc = SubjectClass.builder().classCode(classCode).course(course).roomCode(roomCode)
                .startTime(startTime).build();

        StudentSubjectClass mapping = StudentSubjectClass.builder().mssv(mssv).subjectClass(sc).build();

        when(studentSubjectClassRepository.findUpcomingClasses(eq(dayOfWeek), eq(today), any(LocalTime.class),
                any(LocalTime.class))).thenReturn(List.of(mapping));

        String redisKey = String.format("%s:%s:%s", mssv, classCode, today);
        when(incomingSubjectRepository.existsById(redisKey)).thenReturn(false);

        // Act
        scheduleScheduler.pushNotiForUpcomingClass();

        // Assert
        verify(notificationService).createUpcomingClassNotification(eq(mssv), eq(subjectName), eq(roomCode),
                anyString());
        verify(incomingSubjectRepository).save(any(IncomingSubject.class));
    }

    @Test
    void shouldNotSendNotificationWhenAlreadyNotifiedToday() {
        // Arrange
        LocalTime now = LocalTime.now();
        LocalTime startTime = now.plusMinutes(25);
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue() + 1;

        Course course = Course.builder().courseName(subjectName).build();
        SubjectClass sc = SubjectClass.builder().classCode(classCode).course(course).roomCode(roomCode)
                .startTime(startTime).build();

        StudentSubjectClass mapping = StudentSubjectClass.builder().mssv(mssv).subjectClass(sc).build();

        when(studentSubjectClassRepository.findUpcomingClasses(eq(dayOfWeek), eq(today), any(LocalTime.class),
                any(LocalTime.class))).thenReturn(List.of(mapping));

        String redisKey = String.format("%s:%s:%s", mssv, classCode, today);
        when(incomingSubjectRepository.existsById(redisKey)).thenReturn(true);

        // Act
        scheduleScheduler.pushNotiForUpcomingClass();

        // Assert
        verify(notificationService, never()).createUpcomingClassNotification(anyString(), anyString(), anyString(),
                anyString());
        verify(incomingSubjectRepository, never()).save(any(IncomingSubject.class));
    }

    @Test
    void shouldNotSendNotificationWhenNoUpcomingClasses() {
        // Arrange
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue() + 1;

        when(studentSubjectClassRepository.findUpcomingClasses(eq(dayOfWeek), eq(today), any(LocalTime.class),
                any(LocalTime.class))).thenReturn(List.of());

        // Act
        scheduleScheduler.pushNotiForUpcomingClass();

        // Assert
        verify(notificationService, never()).createUpcomingClassNotification(anyString(), anyString(), anyString(),
                anyString());
    }
}
