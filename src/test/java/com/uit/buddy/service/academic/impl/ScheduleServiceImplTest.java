package com.uit.buddy.service.academic.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.buddy.client.UitClient;
import com.uit.buddy.dto.request.schedule.UploadScheduleRequest;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.schedule.ScheduleMapper;
import com.uit.buddy.service.notification.NotificationService;
import com.uit.buddy.repository.academic.CourseRepository;
import com.uit.buddy.repository.academic.CurriculumCourseRepository;
import com.uit.buddy.repository.academic.MoodleEnrollmentCacheRepository;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.academic.StudentSubjectClassRepository;
import com.uit.buddy.repository.academic.SubjectClassRepository;
import com.uit.buddy.repository.learning.StudentTaskRepository;
import com.uit.buddy.repository.learning.TemporaryDeadlineRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.learning.AssignmentService;
import com.uit.buddy.util.EncryptionUtils;
import com.uit.buddy.util.IcsParser;
import com.uit.buddy.util.IcsParser.ParseResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleServiceImplTest {

    @Mock
    private IcsParser icsParser;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private SubjectClassRepository subjectClassRepository;
    @Mock
    private StudentSubjectClassRepository studentSubjectClassRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CurriculumCourseRepository curriculumCourseRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private UitClient uitClient;
    @Mock
    private EncryptionUtils encryptionUtils;
    @Mock
    private Executor executor;
    @Mock
    private ScheduleMapper scheduleMapper;
    @Mock
    private StudentTaskRepository studentTaskRepository;
    @Mock
    private TemporaryDeadlineRepository temporaryDeadlineRepository;
    @Mock
    private AssignmentService assignmentService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private MoodleEnrollmentCacheRepository enrollmentCache;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private String mssv;
    private Student student;

    @BeforeEach
    void setUp() {
        mssv = "22100001";
        student = Student.builder().mssv(mssv).fullName("Student").email("student@uit.edu.vn").password("pwd")
                .cometUid("uid").homeClassCode("CS01").encryptedWstoken("encrypted").build();
    }

    @Test
    void uploadSchedule_invalidExtension_shouldThrowInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile("icsFile", "schedule.txt", "text/plain", "x".getBytes());
        UploadScheduleRequest request = new UploadScheduleRequest(file);

        assertThatThrownBy(() -> scheduleService.uploadSchedule(mssv, request)).isInstanceOf(ScheduleException.class)
                .extracting("code").isEqualTo(ScheduleErrorCode.INVALID_FILE_TYPE.getCode());

        verify(studentRepository, never()).findById(anyString());
    }

    @Test
    void uploadSchedule_emptyFile_shouldThrowInvalidFileType() {
        MockMultipartFile file = new MockMultipartFile("icsFile", "schedule.ics", "text/calendar", new byte[0]);
        UploadScheduleRequest request = new UploadScheduleRequest(file);

        assertThatThrownBy(() -> scheduleService.uploadSchedule(mssv, request)).isInstanceOf(ScheduleException.class)
                .extracting("code").isEqualTo(ScheduleErrorCode.INVALID_FILE_TYPE.getCode());
    }

    @Test
    void uploadSchedule_validIcsButStudentNotFound_shouldThrowUserException() {
        MockMultipartFile file = new MockMultipartFile("icsFile", "schedule.ics", "text/calendar", "ics".getBytes());
        UploadScheduleRequest request = new UploadScheduleRequest(file);

        when(studentRepository.findById(mssv)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scheduleService.uploadSchedule(mssv, request)).isInstanceOf(UserException.class)
                .extracting("code").isEqualTo(UserErrorCode.STUDENT_NOT_FOUND.getCode());
    }

    @Test
    void uploadSchedule_ownerMismatch_shouldThrowInvalidOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile("icsFile", "schedule.ics", "text/calendar", "ics".getBytes());
        UploadScheduleRequest request = new UploadScheduleRequest(file);

        ParseResult parseResult = new ParseResult();
        parseResult.setStudentId("99999999");
        parseResult.setEvents(List.of());

        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(icsParser.parseIcsFile(any())).thenReturn(parseResult);

        assertThatThrownBy(() -> scheduleService.uploadSchedule(mssv, request)).isInstanceOf(ScheduleException.class)
                .extracting("code").isEqualTo(ScheduleErrorCode.INVALID_OWNER.getCode());
    }

    @Test
    void fetchCourseCalendar_noClasses_shouldThrowIcsFileNotFound() {
        Semester semester = Semester.builder().semesterCode("2024.2").yearStart("2024").semesterNumber(2)
                .startDate(LocalDate.of(2024, 8, 1)).endDate(LocalDate.of(2024, 12, 31)).build();

        when(semesterRepository.findCurrentSemester(any(LocalDate.class))).thenReturn(Optional.of(semester));
        when(studentSubjectClassRepository.findAllByStudentMssvAndSemester(mssv, "2024.2")).thenReturn(List.of());

        assertThatThrownBy(() -> scheduleService.fetchCourseCalendar(mssv, null, null))
                .isInstanceOf(ScheduleException.class).extracting("code")
                .isEqualTo(ScheduleErrorCode.ICS_FILE_NOT_FOUND.getCode());
    }

    @Test
    void fetchCourseCalendar_oneClass_shouldReturnCalendar() {
        List<StudentSubjectClass> classes = List.of(new StudentSubjectClass());
        List<CourseCalendarResponse.Course> mappedCourses = List
                .of(new CourseCalendarResponse.Course("CS101", "CS101.1", "Intro", "Lecturer", 2, "07:30", null, false,
                        "09:00", "1", "3", "A101", "2024-09-01", "2024-12-01", 3, null));

        when(studentSubjectClassRepository.findAllByStudentMssvAndSemester(mssv, "2024.2")).thenReturn(classes);
        when(scheduleMapper.toListCourse(classes)).thenReturn(mappedCourses);

        CourseCalendarResponse result = scheduleService.fetchCourseCalendar(mssv, "2024", "2");

        assertThat(result.countOfCourse()).isEqualTo(1);
        assertThat(result.academicYear()).isEqualTo("2024");
        assertThat(result.semester()).isEqualTo("2");
        assertThat(result.courses()).hasSize(1);
    }

    @Test
    void fetchDeadlinesFromMoodle_zeroDeadline_shouldReturnEmpty() {
        Pageable pageable = PageRequest.of(0, 10);

        when(studentRepository.findById(mssv)).thenReturn(Optional.of(student));
        when(encryptionUtils.decrypt("encrypted")).thenReturn("ws-token");
        when(uitClient.fetchSiteInfo("ws-token")).thenReturn(new SiteInfoResponse(1L, "u", "Student"));
        when(uitClient.getUserCourses("ws-token", 1L)).thenReturn(List.of());

        DeadlineResponse result = scheduleService.fetchDeadlinesFromMoodle(mssv, null, null, pageable);

        assertThat(result.numberOfDeadlines()).isZero();
        assertThat(result.courseContents()).isEmpty();
    }
}
