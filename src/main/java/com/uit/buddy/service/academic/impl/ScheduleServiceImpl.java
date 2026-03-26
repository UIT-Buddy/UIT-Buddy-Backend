package com.uit.buddy.service.academic.impl;

import com.uit.buddy.client.UitClient;
import com.uit.buddy.constant.IcsConstants;
import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.dto.response.client.AssignmentDetailResponse;
import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
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
import com.uit.buddy.mapper.schedule.ScheduleMapper;
import com.uit.buddy.repository.academic.CourseRepository;
import com.uit.buddy.repository.academic.CurriculumCourseRepository;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.academic.StudentSubjectClassRepository;
import com.uit.buddy.repository.academic.SubjectClassRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.util.EncryptionUtils;
import com.uit.buddy.util.IcsParser;
import com.uit.buddy.util.IcsParser.IcsEvent;
import com.uit.buddy.util.IcsParser.ParseResult;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
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
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final SemesterRepository semesterRepository;
    private final UitClient uitClient;
    private final EncryptionUtils encryptionUtils;
    private final Executor executor;
    private final ScheduleMapper scheduleMapper;
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    public ScheduleServiceImpl(IcsParser icsParser, StudentRepository studentRepository,
            SubjectClassRepository subjectClassRepository, StudentSubjectClassRepository studentSubjectClassRepository,
            CourseRepository courseRepository, CurriculumCourseRepository curriculumCourseRepository,
            SemesterRepository semesterRepository, UitClient uitClient, EncryptionUtils encryptionUtils,
            @Qualifier("uploadExecutor") Executor executor, ScheduleMapper scheduleMapper) {
        this.icsParser = icsParser;
        this.studentRepository = studentRepository;
        this.subjectClassRepository = subjectClassRepository;
        this.studentSubjectClassRepository = studentSubjectClassRepository;
        this.courseRepository = courseRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.semesterRepository = semesterRepository;
        this.uitClient = uitClient;
        this.encryptionUtils = encryptionUtils;
        this.executor = executor;
        this.scheduleMapper = scheduleMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CourseCalendarResponse.Course> uploadSchedule(String mssv, UploadScheduleRequest request) {
        log.info("[Schedule Service] Processing sync upload for student: {}", mssv);

        validateIcsFile(request.icsFile());
        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        try {
            ParseResult result = icsParser.parseIcsFile(request.icsFile().getInputStream());
            result.setEvents(removeDuplicateSchedule(mssv, result.getEvents()));
            if (result.getStudentId() != null && !result.getStudentId().equals(mssv)) {
                throw new ScheduleException(ScheduleErrorCode.INVALID_OWNER);
            }

            List<StudentSubjectClass> savedMappings = saveScheduleData(student, result.getEvents());
            List<CourseContentResponse> allDeadlines = fetchCourseDeadlinesFromMoodle(mssv, null, null);
            List<CourseCalendarResponse.Course> courses = scheduleMapper.toListCourseWithDeadlines(savedMappings,
                    allDeadlines);
            courses = sortCourseDeadlineByDueDateDesc(courses);

            log.info("[Schedule Service] Schedule upload successful for student: {}", mssv);

            return courses;

        } catch (ScheduleException e) {
            throw e;
        } catch (IOException e) {
            log.error("[Schedule Service] IO error during sync upload for student {}: ", mssv, e);
            throw new SystemException(SystemErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public CourseCalendarResponse fetchCourseCalendar(String mssv, String year, String semester) {
        String semesterCode = "";
        if (year == null || semester == null) {
            Semester activeSemester = getActiveSemester();
            year = activeSemester.getYearStart();
            semester = activeSemester.getSemesterNumber().toString();
            semesterCode += activeSemester.getSemesterCode();
        } else {
            semesterCode += String.format("%s.%s", year, semester);
        }
        List<StudentSubjectClass> studentClasses = studentSubjectClassRepository.findAllByStudentMssvAndSemester(mssv,
                semesterCode);
        if (studentClasses.isEmpty()) {
            throw new ScheduleException(ScheduleErrorCode.ICS_FILE_NOT_FOUND);
        }
        List<CourseContentResponse> allDeadlines = fetchCourseDeadlinesFromMoodle(mssv, null, null);
        List<CourseCalendarResponse.Course> courses = scheduleMapper.toListCourseWithDeadlines(studentClasses,
                allDeadlines);
        courses = sortCourseDeadlineByDueDateDesc(courses);
        return new CourseCalendarResponse(courses.size(), semester, year, courses);
    }

    @Override
    public DeadlineResponse fetchDeadlinesFromMoodle(String mssv, Integer month, Integer year, Pageable pageable) {
        List<CourseContentResponse> allCourseContents = fetchCourseDeadlinesFromMoodle(mssv, month, year);
        int totalDeadlines = allCourseContents.stream().mapToInt(c -> c.exercises().size()).sum();
        List<CourseContentResponse> pagedCourseContents = paginateDeadlines(allCourseContents, pageable);
        return new DeadlineResponse(totalDeadlines, pagedCourseContents);
    }

    private List<StudentSubjectClass> saveScheduleData(Student student, List<IcsEvent> events) {
        Semester semester = null;
        for (IcsEvent event : events) {
            semester = getSemesterWithEvent(event);
        }
        if (semester == null)
            throw new ScheduleException(ScheduleErrorCode.ICS_UPLOADED);
        Set<String> existingMappingCodes = studentSubjectClassRepository
                .findAllClassCodesByStudentAndSemester(student.getMssv(), semester.getSemesterCode());

        Map<String, IcsEvent> newEventsForStudent = new HashMap<>();
        for (IcsEvent event : events) {
            if (!existingMappingCodes.contains(event.getClassCode())) {
                newEventsForStudent.put(event.getClassCode(), event);
            }
        }

        List<SubjectClass> existingGlobalClasses = subjectClassRepository
                .findAllByClassCodeInAndSemester(newEventsForStudent.keySet(), semester);

        Map<String, SubjectClass> classMap = new HashMap<>();
        existingGlobalClasses.forEach(c -> classMap.put(c.getClassCode(), c));

        List<SubjectClass> classesToCreate = new ArrayList<>();
        List<SubjectClass> classesToUpdate = new ArrayList<>();
        Map<String, Course> courseCache = new HashMap<>();

        for (IcsEvent event : newEventsForStudent.values()) {
            SubjectClass existingClass = classMap.get(event.getClassCode());
            if (existingClass == null) {
                SubjectClass newClass = buildSubjectClassEntity(event, semester, courseCache);
                classesToCreate.add(newClass);
                classMap.put(event.getClassCode(), newClass);
            } else if (applyEventDataToSubjectClass(existingClass, event)) {
                classesToUpdate.add(existingClass);
            }
        }

        if (!classesToCreate.isEmpty()) {
            subjectClassRepository.saveAll(classesToCreate);
        }

        if (!classesToUpdate.isEmpty()) {
            subjectClassRepository.saveAll(classesToUpdate);
        }
        StudentClassStatus classStatus = semester == getActiveSemester() ? StudentClassStatus.STUDYING
                : StudentClassStatus.COMPLETED;
        List<StudentSubjectClass> finalMappings = newEventsForStudent.values().stream()
                .map(event -> StudentSubjectClass.builder().student(student)
                        .subjectClass(classMap.get(event.getClassCode())).status(classStatus).build())
                .toList();
        setCreditsForSubjectClass(student, finalMappings);
        studentSubjectClassRepository.saveAll(finalMappings);
        log.info("[Schedule Service] Successfully synced {} classes for student {}", finalMappings.size(),
                student.getMssv());

        return finalMappings;
    }

    private SubjectClass buildSubjectClassEntity(IcsEvent event, Semester semester, Map<String, Course> courseCache) {
        String courseCode = extractCourseCode(event.getClassCode());

        Course course = courseCache.computeIfAbsent(courseCode,
                code -> courseRepository.findById(code).orElseThrow(() -> {
                    log.error("[Schedule Service] Course not found in database: {}", code);
                    return new ScheduleException(ScheduleErrorCode.COURSE_NOT_FOUND);
                }));
        Integer interval = event.getInterval() != null ? event.getInterval() : 1;
        String classType = Boolean.TRUE.equals(event.getIsBlendedLearning()) ? IcsConstants.BLENDED_LEARNING
                : IcsConstants.WEEKLY;
        return SubjectClass.builder().classCode(event.getClassCode()).course(course).semester(semester)
                .teacherName(event.getTeacherName()).dayOfWeek(event.getDayOfWeek()).startLesson(event.getStartLesson())
                .endLesson(event.getEndLesson()).startTime(event.getStartTime()).endTime(event.getEndTime())
                .startDate(event.getStartDate()).endDate(event.getEndDate()).roomCode(event.getRoomCode())
                .interval(interval).classType(classType).build();
    }

    private boolean applyEventDataToSubjectClass(SubjectClass subjectClass, IcsEvent event) {
        boolean changed = false;

        if (!java.util.Objects.equals(subjectClass.getTeacherName(), event.getTeacherName())) {
            subjectClass.setTeacherName(event.getTeacherName());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getDayOfWeek(), event.getDayOfWeek())) {
            subjectClass.setDayOfWeek(event.getDayOfWeek());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getStartLesson(), event.getStartLesson())) {
            subjectClass.setStartLesson(event.getStartLesson());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getEndLesson(), event.getEndLesson())) {
            subjectClass.setEndLesson(event.getEndLesson());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getStartTime(), event.getStartTime())) {
            subjectClass.setStartTime(event.getStartTime());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getEndTime(), event.getEndTime())) {
            subjectClass.setEndTime(event.getEndTime());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getStartDate(), event.getStartDate())) {
            subjectClass.setStartDate(event.getStartDate());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getEndDate(), event.getEndDate())) {
            subjectClass.setEndDate(event.getEndDate());
            changed = true;
        }
        if (!java.util.Objects.equals(subjectClass.getRoomCode(), event.getRoomCode())) {
            subjectClass.setRoomCode(event.getRoomCode());
            changed = true;
        }

        Integer interval = event.getInterval() != null ? event.getInterval() : 1;
        if (!java.util.Objects.equals(subjectClass.getInterval(), interval)) {
            subjectClass.setInterval(interval);
            changed = true;
        }

        String classType = Boolean.TRUE.equals(event.getIsBlendedLearning()) ? IcsConstants.BLENDED_LEARNING
                : IcsConstants.WEEKLY;
        if (!java.util.Objects.equals(subjectClass.getClassType(), classType)) {
            subjectClass.setClassType(classType);
            changed = true;
        }

        return changed;
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

    private Semester getSemesterWithEvent(IcsEvent event) {
        return semesterRepository.findCurrentSemester(event.getStartDate())
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

    private List<CourseContentResponse> fetchCourseDeadlinesFromMoodle(String mssv, Integer month, Integer year) {
        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));
        String decryptedWstoken = encryptionUtils.decrypt(student.getEncryptedWstoken());
        Map<String, List<CourseDetailResponse>> courseContents = getCourseContents(decryptedWstoken);
        List<CourseContentResponse> deadlines = extractDeadlinesForCourses(courseContents, decryptedWstoken, month,
                year);
        return deadlines;
    }

    private Map<String, List<CourseDetailResponse>> getCourseContents(String wstoken) {
        SiteInfoResponse siteInfo = uitClient.fetchSiteInfo(wstoken);
        Long userId = siteInfo.userid();
        List<EnrolledCourseResponse> enrolledCourseResponses = uitClient.getUserCourses(wstoken, userId);

        List<CompletableFuture<Map.Entry<String, List<CourseDetailResponse>>>> futures = new ArrayList<>();

        for (EnrolledCourseResponse course : enrolledCourseResponses) {
            String courseId = course.id();
            String courseName = course.shortName();

            futures.add(CompletableFuture.supplyAsync(() -> {
                List<CourseDetailResponse> details = uitClient.getAllCourseDetail(wstoken, courseId);
                return Map.entry(courseName, details);
            }, executor));
        }

        return futures.stream().map(CompletableFuture::join)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<CourseContentResponse> extractDeadlinesForCourses(
            Map<String, List<CourseDetailResponse>> courseContents, String decryptedWstoken, Integer month,
            Integer year) {
        List<CompletableFuture<CourseContentResponse>> futures = new ArrayList<>();
        for (Map.Entry<String, List<CourseDetailResponse>> entry : courseContents.entrySet()) {
            String courseName = entry.getKey();
            List<CourseDetailResponse> details = entry.getValue();
            if (hasNoDeadline(details))
                continue;
            futures.add(CompletableFuture
                    .supplyAsync(() -> extractDeadlinesForCourse(courseName, details, decryptedWstoken, month, year)));
        }
        return futures.stream().map(CompletableFuture::join)
                .filter(courseContent -> !courseContent.exercises().isEmpty()).toList();
    }

    private CourseContentResponse extractDeadlinesForCourse(String courseName, List<CourseDetailResponse> details,
            String decryptedWstoken, Integer month, Integer year) {
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
                    LocalDateTime dueDate = toLocalDateTime(dueTimestamp);
                    if (!isMatchedByFilter(dueDate, month, year)) {
                        continue;
                    }

                    exercises.add(new CourseContentResponse.exercise(module.name(), dueDate, module.url(),
                            determineDeadlineStatus(dueDate, decryptedWstoken, module.id())));
                }
            }
        }

        List<CourseContentResponse.exercise> sortedExercises = exercises.stream()
                .sorted(Comparator.comparing(CourseContentResponse.exercise::dueDate).reversed()).toList();

        return new CourseContentResponse(courseName, sortedExercises);
    }

    private boolean isMatchedByFilter(LocalDateTime dueDate, Integer month, Integer year) {
        if (dueDate == null) {
            return false;
        }

        if (year == null) {
            return true;
        }

        if (month == null) {
            return dueDate.getYear() == year;
        }

        return dueDate.getYear() == year && dueDate.getMonthValue() == month;
    }

    private List<CourseContentResponse> paginateDeadlines(List<CourseContentResponse> courseContents,
            Pageable pageable) {
        boolean isDescending = pageable.getSort().stream().findFirst().map(order -> order.isDescending()).orElse(false);
        Comparator<DeadlineEntry> comparator = Comparator.comparing(entry -> entry.exercise().dueDate());
        if (isDescending) {
            comparator = comparator.reversed();
        }

        List<DeadlineEntry> flattened = courseContents.stream().flatMap(
                course -> course.exercises().stream().map(exercise -> new DeadlineEntry(course.courseName(), exercise)))
                .sorted(comparator).toList();

        int start = (int) pageable.getOffset();
        if (start >= flattened.size()) {
            return List.of();
        }

        int end = Math.min(start + pageable.getPageSize(), flattened.size());
        List<DeadlineEntry> paged = flattened.subList(start, end);

        Map<String, List<CourseContentResponse.exercise>> groupedByCourse = new LinkedHashMap<>();
        for (DeadlineEntry entry : paged) {
            groupedByCourse.computeIfAbsent(entry.courseName(), ignored -> new ArrayList<>()).add(entry.exercise());
        }

        return groupedByCourse.entrySet().stream()
                .map(entry -> new CourseContentResponse(entry.getKey(), entry.getValue())).toList();
    }

    private record DeadlineEntry(String courseName, CourseContentResponse.exercise exercise) {
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

    private DeadlineStatus determineDeadlineStatus(LocalDateTime dueDate, String wstoken, String assignmentId) {
        AssignmentDetailResponse assignmentDetail = uitClient.getCourseAssignments(wstoken, assignmentId);
        LocalDateTime now = LocalDateTime.now();
        if (isSubmittedAssignment(assignmentDetail, assignmentId)) {
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

    private boolean isSubmittedAssignment(AssignmentDetailResponse assignmentDetail, String assignmentId) {
        if (assignmentDetail == null || assignmentDetail.lastAttempt() == null
                || assignmentDetail.lastAttempt().submission() == null
                || assignmentDetail.lastAttempt().submission().status() == null) {
            log.warn("[Schedule Service] Missing submission detail for assignmentId={}", assignmentId);
            return false;
        }

        return "submitted".equalsIgnoreCase(assignmentDetail.lastAttempt().submission().status());
    }

    private List<IcsEvent> removeDuplicateSchedule(String mssv, List<IcsEvent> icsEventList) {
        List<IcsEvent> refinedDuplicatedCourse = new ArrayList<>();
        for (IcsEvent icsEvent : icsEventList) {
            if (studentSubjectClassRepository.findSubjectByClassCode(mssv, icsEvent.getClassCode()) == null) {
                refinedDuplicatedCourse.add(icsEvent);
            }
        }
        return refinedDuplicatedCourse;
    }

    private void setCreditsForSubjectClass(Student student, List<StudentSubjectClass> classesToUpdate) {
        String majorCode = resolveMajorCode(student);
        Integer academicStartYear = resolveAcademicStartYear(student);

        for (StudentSubjectClass studentClass : classesToUpdate) {
            SubjectClass subjectClass = studentClass.getSubjectClass();
            if (subjectClass == null) {
                studentClass.setCredits(0);
                continue;
            }

            String courseCode = subjectClass.getCourseCode();
            if ((courseCode == null || courseCode.isBlank()) && subjectClass.getCourse() != null) {
                courseCode = subjectClass.getCourse().getCourseCode();
            }

            if (courseCode == null || courseCode.isBlank()) {
                studentClass.setCredits(0);
                continue;
            }

            if (majorCode == null || majorCode.isBlank() || academicStartYear == null) {
                studentClass.setCredits(0);
                continue;
            }

            Integer credits;
            if (isLabClassCode(subjectClass.getClassCode())) {
                credits = curriculumCourseRepository.findCreditsForLabClass(courseCode, majorCode, academicStartYear);
            } else {
                credits = curriculumCourseRepository.findCreditsForClass(courseCode, majorCode, academicStartYear);
            }
            studentClass.setCredits(credits != null ? credits : 0);
        }
    }

    private boolean isLabClassCode(String classCode) {
        if (classCode == null || classCode.isBlank()) {
            return false;
        }

        int dotCount = 0;
        for (int i = 0; i < classCode.length(); i++) {
            if (classCode.charAt(i) == '.') {
                dotCount++;
            }
        }
        return dotCount >= 2;
    }

    private String resolveMajorCode(Student student) {
        if (student.getHomeClass() != null && student.getHomeClass().getMajorCode() != null
                && !student.getHomeClass().getMajorCode().isBlank()) {
            return student.getHomeClass().getMajorCode();
        }

        String homeClassCode = student.getHomeClassCode();
        if (homeClassCode != null && homeClassCode.length() >= 4) {
            return homeClassCode.substring(0, 4);
        }

        return null;
    }

    private Integer resolveAcademicStartYear(Student student) {
        if (student.getHomeClass() != null && student.getHomeClass().getAcademicYear() != null) {
            Matcher matcher = YEAR_PATTERN.matcher(student.getHomeClass().getAcademicYear());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        String mssv = student.getMssv();
        if (mssv != null && mssv.length() >= 2 && mssv.substring(0, 2).chars().allMatch(Character::isDigit)) {
            return 2000 + Integer.parseInt(mssv.substring(0, 2));
        }

        return null;
    }

    private List<CourseCalendarResponse.Course> sortCourseDeadlineByDueDateDesc(
            List<CourseCalendarResponse.Course> courses) {
        return courses.stream().map(course -> {
            if (course.deadline() == null || course.deadline().exercises() == null) {
                return course;
            }

            List<CourseContentResponse.exercise> sortedExercises = course.deadline().exercises().stream()
                    .sorted(Comparator.comparing(CourseContentResponse.exercise::dueDate).reversed()).toList();

            CourseContentResponse sortedDeadline = new CourseContentResponse(course.deadline().courseName(),
                    sortedExercises);

            return new CourseCalendarResponse.Course(course.courseCode(), course.classId(), course.courseName(),
                    course.lecturer(), course.dayOfWeek(), course.startTime(), course.labOfClassId(),
                    course.isBlendedLearning(), course.endTime(), course.startPeriod(), course.endPeriod(),
                    course.roomCode(), course.startDate(), course.endDate(), course.credits(), sortedDeadline);
        }).toList();
    }
}
