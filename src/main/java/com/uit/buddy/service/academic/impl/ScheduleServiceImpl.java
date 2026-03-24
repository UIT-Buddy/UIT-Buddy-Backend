package com.uit.buddy.service.academic.impl;

import com.uit.buddy.client.UitClient;
import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.dto.response.client.AssignmentDetailResponse;
import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.entity.academic.Course;
import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.DeadlineStatus;
import com.uit.buddy.enums.StudentClassStatus;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.repository.academic.CourseRepository;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.academic.StudentSubjectClassRepository;
import com.uit.buddy.repository.academic.SubjectClassRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.util.EncryptionUtils;
import com.uit.buddy.util.IcsParser;
import com.uit.buddy.util.IcsParser.IcsEvent;
import com.uit.buddy.util.IcsParser.ParseResult;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final IcsParser icsParser;
    private final StudentRepository studentRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final StudentSubjectClassRepository studentSubjectClassRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final UitClient uitClient;
    private final EncryptionUtils encryptionUtils;
    private final Executor executor;

    public ScheduleServiceImpl(IcsParser icsParser, StudentRepository studentRepository,
            SubjectClassRepository subjectClassRepository, StudentSubjectClassRepository studentSubjectClassRepository,
            CourseRepository courseRepository, SemesterRepository semesterRepository, UitClient uitClient,
            EncryptionUtils encryptionUtils, @Qualifier("uploadExecutor") Executor executor) {
        this.icsParser = icsParser;
        this.studentRepository = studentRepository;
        this.subjectClassRepository = subjectClassRepository;
        this.studentSubjectClassRepository = studentSubjectClassRepository;
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.uitClient = uitClient;
        this.encryptionUtils = encryptionUtils;
        this.executor = executor;
    }

    @Transactional(rollbackFor = Exception.class)
    public void uploadSchedule(String mssv, UploadScheduleRequest request) {
        log.info("[Schedule Service] Processing sync upload for student: {}", mssv);

        validateIcsFile(request.icsFile());

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        try {
            ParseResult result = icsParser.parseIcsFile(request.icsFile().getInputStream());

            if (result.getStudentId() != null && !result.getStudentId().equals(mssv)) {
                throw new ScheduleException(ScheduleErrorCode.INVALID_OWNER);
            }

            saveScheduleData(student, result.getEvents());

            log.info("[Schedule Service] Schedule upload successful for student: {}", mssv);

        } catch (ScheduleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Schedule Service] Critical error during sync upload for student {}: ", mssv, e);
            throw new SystemException(SystemErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public DeadlineResponse fetchDeadlinesFromMoodle(String mssv) {
        List<CourseContentResponse> courseContents = fetchCourseDeadlinesFromMoodle(mssv);
        int numberOfDeadlines = courseContents.stream().mapToInt(c -> c.exercises().size()).sum();
        return new DeadlineResponse(numberOfDeadlines, courseContents);
    }

    private void saveScheduleData(Student student, List<IcsEvent> events) {
        Semester semester = getActiveSemester();

        Set<String> existingMappingCodes = studentSubjectClassRepository
                .findAllClassCodesByStudentAndSemester(student.getMssv(), semester.getSemesterCode());

        Map<String, IcsEvent> newEventsForStudent = new HashMap<>();
        for (IcsEvent event : events) {
            if (!existingMappingCodes.contains(event.getClassCode())) {
                newEventsForStudent.put(event.getClassCode(), event);
            }
        }
        if (newEventsForStudent.isEmpty())
            return;

        List<SubjectClass> existingGlobalClasses = subjectClassRepository
                .findAllByClassCodeInAndSemester(newEventsForStudent.keySet(), semester);

        Map<String, SubjectClass> classMap = new HashMap<>();
        existingGlobalClasses.forEach(c -> classMap.put(c.getClassCode(), c));

        List<SubjectClass> classesToCreate = new ArrayList<>();
        Map<String, Course> courseCache = new HashMap<>();

        for (IcsEvent event : newEventsForStudent.values()) {
            if (!classMap.containsKey(event.getClassCode())) {
                SubjectClass newClass = buildSubjectClassEntity(event, semester, courseCache);
                classesToCreate.add(newClass);
                classMap.put(event.getClassCode(), newClass);
            }
        }

        if (!classesToCreate.isEmpty()) {
            subjectClassRepository.saveAll(classesToCreate);
        }

        List<StudentSubjectClass> finalMappings = newEventsForStudent.values().stream()
                .map(event -> StudentSubjectClass.builder().student(student)
                        .subjectClass(classMap.get(event.getClassCode())).status(StudentClassStatus.STUDYING).build())
                .toList();

        studentSubjectClassRepository.saveAll(finalMappings);
        log.info("[Schedule Service] Successfully synced {} classes for student {}", finalMappings.size(),
                student.getMssv());
    }

    private SubjectClass buildSubjectClassEntity(IcsEvent event, Semester semester, Map<String, Course> courseCache) {
        String courseCode = extractCourseCode(event.getClassCode());

        Course course = courseCache.computeIfAbsent(courseCode,
                code -> courseRepository.findById(code).orElseThrow(() -> {
                    log.error("[Schedule Service] Course not found in database: {}", code);
                    return new ScheduleException(ScheduleErrorCode.COURSE_NOT_FOUND);
                }));

        return SubjectClass.builder().classCode(event.getClassCode()).course(course).semester(semester)
                .teacherName(event.getTeacherName()).dayOfWeek(event.getDayOfWeek()).startTime(event.getStartTime())
                .endTime(event.getEndTime()).startLesson(event.getStartLesson()).endLesson(event.getEndLesson())
                .roomCode(event.getRoomCode()).interval(event.getInterval() != null ? event.getInterval() : 1).build();
    }

    private String extractCourseCode(String classCode) {
        if (classCode != null && classCode.contains(".")) {
            return classCode.substring(0, classCode.indexOf("."));
        }
        return classCode;
    }

    private Semester getActiveSemester() {
        return semesterRepository.findCurrentSemester(LocalDate.now())
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SEMESTER_NOT_FOUND));
    }

    private void validateIcsFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".ics")) {
            log.error("[Schedule Service] Invalid file format: {}", filename);
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }
    }

    private List<CourseContentResponse> fetchCourseDeadlinesFromMoodle(String mssv) {
        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));
        String decryptedWstoken = encryptionUtils.decrypt(student.getEncryptedWstoken());
        Map<String, List<CourseDetailResponse>> courseContents = getCourseContents(decryptedWstoken);
        List<CourseContentResponse> deadlines = extractDeadlinesForCourses(courseContents, decryptedWstoken);
        return deadlines;
    }

    private Map<String, List<CourseDetailResponse>> getCourseContents(String wstoken) {
        SiteInfoResponse siteInfo = uitClient.fetchSiteInfo(wstoken);
        Long userId = siteInfo.userid();
        List<EnrolledCourseResponse> enrolledCourseResponses = uitClient.getUserCourses(wstoken, userId);
        System.out.println("BUG HERE");
        Map<String, String> coursesInSemester = getCourseSemesters(enrolledCourseResponses);

        List<CompletableFuture<Map.Entry<String, List<CourseDetailResponse>>>> futures = new ArrayList<>();

        for (Map.Entry<String, String> entry : coursesInSemester.entrySet()) {
            String courseId = entry.getKey();
            String courseName = entry.getValue();

            futures.add(CompletableFuture.supplyAsync(() -> {
                List<CourseDetailResponse> details = uitClient.getAllCourseDetail(wstoken, courseId);
                return Map.entry(courseName, details);
            }, executor));
        }

        return futures.stream().map(CompletableFuture::join)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<CourseContentResponse> extractDeadlinesForCourses(
            Map<String, List<CourseDetailResponse>> courseContents, String decryptedWstoken) {
        List<CompletableFuture<CourseContentResponse>> futures = new ArrayList<>();
        for (Map.Entry<String, List<CourseDetailResponse>> entry : courseContents.entrySet()) {
            String courseName = entry.getKey();
            List<CourseDetailResponse> details = entry.getValue();
            if (hasNoDeadline(details))
                continue;
            futures.add(CompletableFuture
                    .supplyAsync(() -> extractDeadlinesForCourse(courseName, details, decryptedWstoken)));
        }
        return futures.stream().map(CompletableFuture::join).toList();
    }

    private CourseContentResponse extractDeadlinesForCourse(String courseName, List<CourseDetailResponse> details,
            String decryptedWstoken) {
        List<CourseContentResponse.exercise> exercises = new ArrayList<>();

        for (CourseDetailResponse detail : details) {
            if (hasNoDeadline(detail)) {
                continue;
            }

            for (CourseDetailResponse.CourseDetailModuleResponse module : detail.moduleResponse()) {
                if (module.dates() == null) {
                    continue;
                }
                String dueTimestamp = module.dates().stream().filter(d -> "Due:".equalsIgnoreCase(d.label())).map(
                        CourseDetailResponse.CourseDetailModuleResponse.CourseDetailModuleDatesResonponse::timestamp)
                        .findFirst().orElse(null);

                if (dueTimestamp != null) {
                    exercises.add(new CourseContentResponse.exercise(module.name(), toLocalDateTime(dueTimestamp),
                            module.url(),
                            determineDeadlineStatus(toLocalDateTime(dueTimestamp), decryptedWstoken, module.id())));
                }
            }
        }

        return new CourseContentResponse(courseName, exercises);
    }

    private boolean hasNoDeadline(List<CourseDetailResponse> details) {
        for (CourseDetailResponse detail : details) {
            if (!hasNoDeadline(detail)) {
                return false;
            }
        }
        return true;
    }

    private boolean hasNoDeadline(CourseDetailResponse detail) {
        if (detail.moduleResponse() == null)
            return true;
        for (var module : detail.moduleResponse()) {
            if (module.dates() != null) {
                for (var date : module.dates()) {
                    if ("Due:".equalsIgnoreCase(date.label())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private LocalDateTime toLocalDateTime(String timestamp) {
        if (timestamp == null)
            return null;

        long epochSeconds = Long.parseLong(timestamp);

        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
    }

    private Map<String, String> getCourseSemesters(List<EnrolledCourseResponse> courses) {
        Map<String, String> coursesInSemester = new HashMap<>();
        for (EnrolledCourseResponse course : courses) {
            if (verifySemester(course.startDate())) {
                coursesInSemester.put(course.id(), course.shortName());
            }
        }
        return coursesInSemester;
    }

    private boolean verifySemester(String startDate) {
        long courseTs = Long.parseLong(startDate);
        long nowTs = Instant.now().getEpochSecond();

        var courseDate = Instant.ofEpochSecond(courseTs).atZone(ZoneId.systemDefault());

        var nowDate = Instant.ofEpochSecond(nowTs).atZone(ZoneId.systemDefault());

        int courseYear = courseDate.getYear();
        int nowYear = nowDate.getYear();

        int courseMonth = courseDate.getMonthValue();
        int nowMonth = nowDate.getMonthValue();

        int courseSemester = (courseMonth <= 6) ? 1 : 2;
        int currentSemester = (nowMonth <= 6) ? 1 : 2;

        return courseYear == nowYear && courseSemester == currentSemester;
    }

    private DeadlineStatus determineDeadlineStatus(LocalDateTime dueDate, String wstoken, String assignmentId) {
        AssignmentDetailResponse assignmentDetail = uitClient.getCourseAssignments(wstoken, assignmentId);
        LocalDateTime now = LocalDateTime.now();
        if (assignmentDetail.lastAttempt().submission().status().equalsIgnoreCase("submitted")) {
            return DeadlineStatus.DONE;
        }
        if (dueDate.isBefore(now)) {
            return DeadlineStatus.OVERDUE;
        }
        if (dueDate.isBefore(now.plusHours(24))) {
            return DeadlineStatus.NEARDEADLINE;
        }
        return DeadlineStatus.UPCOMING;

    }
}
