package com.uit.buddy.service.academic.impl;

import com.uit.buddy.client.UitClient;
import com.uit.buddy.constant.IcsConstants;
import com.uit.buddy.constant.ScheduleConstant;
import com.uit.buddy.dto.request.schedule.CreateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UpdateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UploadScheduleRequest;
import com.uit.buddy.dto.response.client.AssignmentDetailResponse;
import com.uit.buddy.dto.response.client.CourseDetailResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.CreateDeadlineResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.entity.academic.Course;
import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.entity.learning.StudentTask;
import com.uit.buddy.entity.learning.TemporaryDeadline;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.DeadlineStatus;
import com.uit.buddy.enums.StudentClassStatus;
import com.uit.buddy.enums.TaskType;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.schedule.ScheduleMapper;
import com.uit.buddy.repository.academic.CourseRepository;
import com.uit.buddy.repository.academic.CurriculumCourseRepository;
import com.uit.buddy.repository.academic.MoodleEnrollmentCacheRepository;
import com.uit.buddy.repository.academic.SemesterRepository;
import com.uit.buddy.repository.academic.StudentSubjectClassRepository;
import com.uit.buddy.repository.academic.SubjectClassRepository;
import com.uit.buddy.repository.learning.StudentTaskRepository;
import com.uit.buddy.repository.learning.TemporaryDeadlineRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.service.learning.AssignmentService;
import com.uit.buddy.service.notification.NotificationService;
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
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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
    private final StudentTaskRepository studentTaskRepository;
    private final TemporaryDeadlineRepository temporaryDeadlineRepository;
    private final SemesterRepository semesterRepository;
    private final AssignmentService assignmentService;
    private final NotificationService notificationService;
    private final MoodleEnrollmentCacheRepository enrollmentCache;
    private final UitClient uitClient;
    private final EncryptionUtils encryptionUtils;
    private final Executor executor;
    private final ScheduleMapper scheduleMapper;
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})");

    public ScheduleServiceImpl(IcsParser icsParser, StudentRepository studentRepository,
            SubjectClassRepository subjectClassRepository, StudentSubjectClassRepository studentSubjectClassRepository,
            CourseRepository courseRepository, CurriculumCourseRepository curriculumCourseRepository,
            AssignmentService assignmentService, SemesterRepository semesterRepository,
            TemporaryDeadlineRepository temporaryDeadlineRepository, NotificationService notificationService,
            MoodleEnrollmentCacheRepository enrollmentCache, UitClient uitClient, EncryptionUtils encryptionUtils,
            StudentTaskRepository studentTaskRepository, @Qualifier("uploadExecutor") Executor executor,
            ScheduleMapper scheduleMapper) {
        this.icsParser = icsParser;
        this.studentRepository = studentRepository;
        this.subjectClassRepository = subjectClassRepository;
        this.studentSubjectClassRepository = studentSubjectClassRepository;
        this.courseRepository = courseRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.semesterRepository = semesterRepository;
        this.temporaryDeadlineRepository = temporaryDeadlineRepository;
        this.notificationService = notificationService;
        this.enrollmentCache = enrollmentCache;
        this.uitClient = uitClient;
        this.encryptionUtils = encryptionUtils;
        this.executor = executor;
        this.scheduleMapper = scheduleMapper;
        this.studentTaskRepository = studentTaskRepository;
        this.assignmentService = assignmentService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CourseCalendarResponse.Course> uploadSchedule(String mssv, UploadScheduleRequest request) {
        log.info("[Schedule Service] Processing ICS upload for student: {}", mssv);

        validateIcsFile(request.icsFile());
        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        try {
            ParseResult result = icsParser.parseIcsFile(request.icsFile().getInputStream());
            result.setEvents(removeDuplicateSchedule(result.getEvents()));
            if (result.getStudentId() != null && !result.getStudentId().equals(mssv)) {
                throw new ScheduleException(ScheduleErrorCode.INVALID_OWNER);
            }

            // Save schedule from ICS — no Moodle calls here, just fast file parsing + DB
            // write
            List<StudentSubjectClass> savedMappings = saveScheduleData(student, result.getEvents());
            List<CourseCalendarResponse.Course> courses = scheduleMapper.toListCourse(savedMappings);
            courses = sortCoursesByClassCode(courses);

            log.info("[Schedule Service] Schedule upload successful for student: {}", mssv);

            return courses;

        } catch (ScheduleException e) {
            throw e;
        } catch (IOException e) {
            log.error("[Schedule Service] IO error during ICS upload for student {}: ", mssv, e);
            throw new SystemException(SystemErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public List<CourseCalendarResponse.Course> syncAssignments(String mssv, Integer month, Integer year) {
        log.info("[Schedule Service] Syncing assignments from Moodle for student: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        String decryptedWstoken = encryptionUtils.decrypt(student.getEncryptedWstoken());
        Map<String, List<CourseDetailResponse>> courseContents = getCourseContents(decryptedWstoken, mssv);
        List<CourseContentResponse> deadlines = extractDeadlinesForCourses(mssv, courseContents, decryptedWstoken,
                month, year);
        List<StudentSubjectClass> savedMappings = studentSubjectClassRepository.findSubjectsByMssv(mssv);
        List<CourseCalendarResponse.Course> courses = scheduleMapper.toListCourseWithDeadlines(savedMappings,
                deadlines);
        log.warn("Can not find subject for student: {}", mssv);
        courses = sortCourseDeadlineByDueDateDesc(courses);

        log.info("[Schedule Service] Assignment sync complete for student: {} ({} courses with deadlines)", mssv,
                courses.size());

        return courses;
    }

    @Override
    public CourseContentResponse syncCourseAssignments(String mssv, String classId, Integer month, Integer year) {
        log.info("[Schedule Service] Syncing assignments for classId={} student={}", classId, mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        String decryptedWstoken = encryptionUtils.decrypt(student.getEncryptedWstoken());

        // Find the Moodle course ID from enrolled courses (cached)
        EnrolledCoursesResult result = getCachedEnrolledCourses(decryptedWstoken, mssv);
        List<EnrolledCourseResponse> enrolledCourses = result.enrolledCourses();

        EnrolledCourseResponse targetCourse = enrolledCourses.stream()
                .filter(c -> c.shortName().equalsIgnoreCase(classId)).findFirst()
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.CLASS_NOT_FOUND));

        // Fetch course detail + batch-fetch submission statuses for this one course
        List<CourseDetailResponse> details = uitClient.getAllCourseDetail(decryptedWstoken, targetCourse.id());
        CourseContentResponse deadlines = extractDeadlinesForCourse(classId, details, decryptedWstoken, month, year);

        log.info("[Schedule Service] Assignment sync for classId={} done ({} exercises)", classId,
                deadlines.exercises().size());

        return deadlines;
    }

    @Override
    public List<String> fetchStudyingClassCodes(String mssv) {
        studentRepository.findById(mssv).orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));
        return studentSubjectClassRepository.findDistinctClassCodesByStudentAndStatus(mssv, StudentClassStatus.STUDYING)
                .stream().filter(classCode -> classCode != null && !classCode.isBlank()).sorted().toList();
    }

    @Override
    public CreateDeadlineResponse createDeadline(String mssv, CreateDeadlineRequest request) {
        if (request.exerciseName() == null)
            throw new ScheduleException(ScheduleErrorCode.INVALID_EXERCISE_NAME);
        if (request.dueDate() == null)
            throw new ScheduleException(ScheduleErrorCode.INVALID_DUE_TIME);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        StudentSubjectClass studentSubjectClass = resolveStudentSubjectClass(mssv, request.classCode());
        TaskType taskType = studentSubjectClass == null ? TaskType.PERSONAL : TaskType.ASSIGNMENT;
        SubjectClass subjectClass = subjectClassRepository.findByClassCodeAndStudentMssv(student.getMssv(),
                request.classCode());

        log.info("[SCHEDULE SERVICE]: Create task for user with id {}", mssv);
        StudentTask studentTask = StudentTask.builder().student(student).taskType(taskType).subjectClass(subjectClass)
                .personalTitle(request.exerciseName()).reminderAt(request.dueDate()).build();
        studentTaskRepository.save(studentTask);
        return scheduleMapper.toCreateDeadlineResponse(studentTask);
    }

    private StudentSubjectClass resolveStudentSubjectClass(String mssv, String classCode) {
        if (classCode == null || classCode.isBlank()) {
            return null;
        }

        StudentSubjectClass studentSubjectClass = studentSubjectClassRepository.findSubjectByClassCode(mssv, classCode);
        if (studentSubjectClass == null) {
            throw new ScheduleException(ScheduleErrorCode.CLASS_NOT_FOUND);
        }

        return studentSubjectClass;
    }

    @Override
    public DeadlineResponse fetchDeadline(String mssv, Integer month, Integer year, Pageable pageable) {
        Semester semester = getActiveSemester();
        String semesterCode = semester.getSemesterCode();

        // Read from TemporaryDeadline table scoped to semester and (optionally)
        // month/year
        List<TemporaryDeadline> savedDeadlines = (month != null && year != null)
                ? temporaryDeadlineRepository.findByMssvAndSemesterCodeAndMonthAndYear(mssv, semesterCode, month, year)
                : temporaryDeadlineRepository.findByMssvAndSemesterCodeAll(mssv, semesterCode);

        List<CourseContentResponse> moodleDeadlines = savedDeadlines.stream()
                .collect(Collectors.groupingBy(TemporaryDeadline::getClassCode)).entrySet().stream()
                .map(entry -> new CourseContentResponse(entry.getKey(), entry.getValue().stream()
                        .map(td -> new CourseContentResponse.exercise(td.getId(), td.getDeadlineName(), td.getDueDate(),
                                td.getUrl(), td.getStatus() != null ? td.getStatus() : DeadlineStatus.UPCOMING, false))
                        .toList()))
                .toList();

        // Read from StudentTask (personal/course-linked) scoped to current semester
        List<CourseContentResponse> studentTasks = getCurrentSemesterDeadlines(mssv, month, year);

        // Merge and paginate
        List<CourseContentResponse> joinedTask = new ArrayList<>(moodleDeadlines);
        joinedTask.addAll(studentTasks);
        int totalDeadlines = joinedTask.stream().mapToInt(c -> c.exercises().size()).sum();
        List<CourseContentResponse> pagedCourseContents = paginateDeadlines(joinedTask, pageable);
        return new DeadlineResponse(totalDeadlines, pagedCourseContents);
    }

    /**
     * Fetches personal/course-linked deadlines from StudentTask table that fall within the given semester's date range.
     * Delegates to {@link AssignmentService} which uses a proper JOIN FETCH query, avoiding LazyInitializationException
     * on SubjectClass.
     */
    private List<CourseContentResponse> getCurrentSemesterDeadlines(String mssv, Integer month, Integer year) {
        return assignmentService.getDeadlineWithMssv(mssv, month, year);
    }

    private void syncDeadlinesToTable(String mssv, List<CourseContentResponse> freshDeadlines) {
        Semester semester = getActiveSemester();
        String semesterCode = semester != null ? semester.getSemesterCode() : null;

        List<TemporaryDeadline> existing = temporaryDeadlineRepository.findByMssv(mssv);
        Map<String, TemporaryDeadline> existingMap = existing.stream().collect(java.util.stream.Collectors
                .toMap(td -> buildDeadlineKey(td.getClassCode(), td.getDeadlineName(), td.getDueDate()), td -> td));

        List<TemporaryDeadline> toSave = new ArrayList<>();
        for (CourseContentResponse course : freshDeadlines) {
            String classCode = course.courseName() == null || course.courseName().isBlank()
                    ? ScheduleConstant.UNKNOWN_CLASS_CODE : course.courseName();
            for (CourseContentResponse.exercise exercise : course.exercises()) {
                if (exercise.dueDate() == null || exercise.exerciseName() == null || exercise.exerciseName().isBlank())
                    continue;
                // Always resolve status from due date (mirrors mapDeadlineStatus in
                // ScheduleMapper)
                String key = buildDeadlineKey(classCode, exercise.exerciseName(), exercise.dueDate());
                TemporaryDeadline existingTd = existingMap.get(key);
                if (existingTd == null) {
                    toSave.add(TemporaryDeadline.builder().mssv(mssv).classCode(classCode)
                            .deadlineName(exercise.exerciseName()).dueDate(exercise.dueDate()).status(exercise.status())
                            .semesterCode(semesterCode).url(exercise.url()).build());
                } else {
                    boolean updated = false;
                    if (!Objects.equals(existingTd.getStatus(), exercise.status())) {
                        existingTd.setStatus(exercise.status());
                        updated = true;
                    }
                    if (!Objects.equals(existingTd.getUrl(), exercise.url())) {
                        existingTd.setUrl(exercise.url());
                        updated = true;
                    }
                    if (updated) {
                        toSave.add(existingTd);
                    }
                }
            }
        }
        if (!toSave.isEmpty()) {
            temporaryDeadlineRepository.saveAll(toSave);
        }
    }

    private String buildDeadlineKey(String classCode, String deadlineName, LocalDateTime dueDate) {
        return String.format("%s|%s|%s", classCode == null ? "" : classCode.trim().toLowerCase(),
                deadlineName == null ? "" : deadlineName.trim().toLowerCase(), dueDate);
    }

    @Override
    @Transactional
    public CreateDeadlineResponse updateDeadline(String mssv, UpdateDeadlineRequest request) {
        // Try StudentTask first
        var studentTaskOpt = studentTaskRepository.findByIdAndMssv(request.studentTaskId(), mssv);
        if (studentTaskOpt.isPresent()) {
            StudentTask studentTask = studentTaskOpt.get();
            if (request.exerciseName() != null)
                studentTask.setPersonalTitle(request.exerciseName());
            if (request.dueDate() != null)
                studentTask.setReminderAt(request.dueDate());
            if (request.status() != null) {
                studentTask.setIsCompleted(request.status() == DeadlineStatus.DONE);
            }
            studentTaskRepository.save(studentTask);
            return scheduleMapper.toCreateDeadlineResponse(studentTask);
        }

        // Try TemporaryDeadline
        var temporaryDeadlineOpt = temporaryDeadlineRepository.findById(request.studentTaskId());
        if (temporaryDeadlineOpt.isPresent() && temporaryDeadlineOpt.get().getMssv().equals(mssv)) {
            TemporaryDeadline td = temporaryDeadlineOpt.get();
            if (request.exerciseName() != null)
                td.setDeadlineName(request.exerciseName());
            if (request.dueDate() != null)
                td.setDueDate(request.dueDate());
            if (request.status() != null) {
                td.setStatus(request.status());
            }
            temporaryDeadlineRepository.save(td);

            // Map TemporaryDeadline back to CreateDeadlineResponse
            return new CreateDeadlineResponse(td.getId(), td.getClassCode(), false, td.getDeadlineName(),
                    td.getDueDate(), td.getStatus());
        }

        throw new ScheduleException(ScheduleErrorCode.ASSIGNMENT_NOT_EXIST);
    }

    @Override
    public CreateDeadlineResponse getDeadlineDetail(String mssv, UUID deadlineId) {
        StudentTask studentTask = studentTaskRepository.findByIdAndMssv(deadlineId, mssv)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.ASSIGNMENT_NOT_EXIST));

        return scheduleMapper.toCreateDeadlineResponse(studentTask);
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
        List<CourseCalendarResponse.Course> courses = scheduleMapper.toListCourse(studentClasses);
        courses = sortCoursesByClassCode(courses);
        return new CourseCalendarResponse(courses.size(), semester, year, courses);
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
        StudentClassStatus classStatus = resolveClassStatus(semester);
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

    private StudentClassStatus resolveClassStatus(Semester semester) {
        Semester activeSemester = getActiveSemester();
        boolean isActiveSemester = semester != null && activeSemester != null
                && Objects.equals(semester.getSemesterCode(), activeSemester.getSemesterCode());
        return isActiveSemester ? StudentClassStatus.STUDYING : StudentClassStatus.COMPLETED;
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

        if (!Objects.equals(subjectClass.getTeacherName(), event.getTeacherName())) {
            subjectClass.setTeacherName(event.getTeacherName());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getDayOfWeek(), event.getDayOfWeek())) {
            subjectClass.setDayOfWeek(event.getDayOfWeek());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getStartLesson(), event.getStartLesson())) {
            subjectClass.setStartLesson(event.getStartLesson());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getEndLesson(), event.getEndLesson())) {
            subjectClass.setEndLesson(event.getEndLesson());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getStartTime(), event.getStartTime())) {
            subjectClass.setStartTime(event.getStartTime());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getEndTime(), event.getEndTime())) {
            subjectClass.setEndTime(event.getEndTime());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getStartDate(), event.getStartDate())) {
            subjectClass.setStartDate(event.getStartDate());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getEndDate(), event.getEndDate())) {
            subjectClass.setEndDate(event.getEndDate());
            changed = true;
        }
        if (!Objects.equals(subjectClass.getRoomCode(), event.getRoomCode())) {
            subjectClass.setRoomCode(event.getRoomCode());
            changed = true;
        }

        Integer interval = event.getInterval() != null ? event.getInterval() : 1;
        if (!Objects.equals(subjectClass.getInterval(), interval)) {
            subjectClass.setInterval(interval);
            changed = true;
        }

        String classType = Boolean.TRUE.equals(event.getIsBlendedLearning()) ? IcsConstants.BLENDED_LEARNING
                : IcsConstants.WEEKLY;
        if (!Objects.equals(subjectClass.getClassType(), classType)) {
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

    @Override
    public List<CourseContentResponse> fetchCourseDeadlinesFromMoodle(String mssv, Integer month, Integer year) {
        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));
        String decryptedWstoken = encryptionUtils.decrypt(student.getEncryptedWstoken());
        return fetchCourseDeadlinesFromMoodleWithToken(mssv, decryptedWstoken, month, year);
    }

    private List<CourseContentResponse> fetchCourseDeadlinesFromMoodleWithToken(String mssv, String decryptedWstoken,
            Integer month, Integer year) {
        Map<String, List<CourseDetailResponse>> courseContents = getCourseContents(decryptedWstoken, mssv);
        return extractDeadlinesForCourses(mssv, courseContents, decryptedWstoken, month, year);
    }

    /**
     * Returns (userId, enrolledCourses) from Redis cache if present; otherwise fetches from Moodle and caches for
     * MOODLE_ENROLLMENT_CACHE_TTL_SECONDS. Falls back to a live Moodle call on any error.
     */
    private EnrolledCoursesResult getCachedEnrolledCourses(String decryptedWstoken, String mssv) {
        return enrollmentCache.findByMssv(mssv)
                .map(cache -> new EnrolledCoursesResult(cache.userId(), cache.enrolledCourses())).orElseGet(() -> {
                    log.info("[Schedule Service] Enrollment cache miss for mssv={}, fetching from Moodle", mssv);
                    SiteInfoResponse siteInfo = uitClient.fetchSiteInfo(decryptedWstoken);
                    Long userId = siteInfo.userid();
                    List<EnrolledCourseResponse> enrolledCourses = uitClient.getUserCourses(decryptedWstoken, userId);
                    enrollmentCache.save(mssv, userId, enrolledCourses);
                    return new EnrolledCoursesResult(userId, enrolledCourses);
                });
    }

    private record EnrolledCoursesResult(Long userId, List<EnrolledCourseResponse> enrolledCourses) {
    }

    private Map<String, List<CourseDetailResponse>> getCourseContents(String wstoken, String mssv) {
        EnrolledCoursesResult result = getCachedEnrolledCourses(wstoken, mssv);
        List<EnrolledCourseResponse> enrolledCourseResponses = result.enrolledCourses();

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

    private List<CourseContentResponse> extractDeadlinesForCourses(String mssv,
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
        List<CourseContentResponse> freshDeadlines = futures.stream().map(CompletableFuture::join)
                .filter(courseContent -> !courseContent.exercises().isEmpty()).toList();
        if (!freshDeadlines.isEmpty()) {
            syncDeadlinesToTable(mssv, freshDeadlines);
        }
        return freshDeadlines;
    }

    private CourseContentResponse extractDeadlinesForCourse(String courseName, List<CourseDetailResponse> details,
            String decryptedWstoken, Integer month, Integer year) {
        List<ModuleWithDate> modulesWithDueDates = new ArrayList<>();
        for (CourseDetailResponse detail : details) {
            if (detailHasDeadline(detail)) {
                continue;
            }

            for (CourseDetailResponse.CourseDetailModuleResponse module : detail.moduleResponse()) {
                if (module.dates() == null) {
                    continue;
                }
                String dueTimestamp = module.dates().stream()
                        .filter(d -> ScheduleConstant.DUE_DATE_LABEL.equalsIgnoreCase(d.label()))
                        .map(CourseDetailResponse.CourseDetailModuleResponse.CourseDetailModuleDatesResonponse::timestamp)
                        .findFirst().orElse(null);

                if (dueTimestamp == null) {
                    continue;
                }

                LocalDateTime dueDate = toLocalDateTime(dueTimestamp);
                if (!isMatchedByFilter(dueDate, month, year)) {
                    continue;
                }

                modulesWithDueDates.add(new ModuleWithDate(module.id(), module.name(), module.url(), dueDate));
            }
        }

        if (modulesWithDueDates.isEmpty()) {
            return new CourseContentResponse(courseName, List.of());
        }

        // ── Pass 2: batch-fetch submission statuses for ALL modules in this course ──
        List<String> assignmentIds = modulesWithDueDates.stream().map(m -> m.id).toList();
        Map<String, AssignmentDetailResponse> submissionStatuses = uitClient.getAssignmentsInfo(decryptedWstoken,
                assignmentIds);

        // ── Pass 3: resolve deadline status using cached results ────────────────────
        List<CourseContentResponse.exercise> exercises = modulesWithDueDates.stream().map(m -> {
            DeadlineStatus status = determineDeadlineStatusFromCache(m.dueDate, submissionStatuses.get(m.id));
            return new CourseContentResponse.exercise(null, m.name, m.dueDate, m.url, status, false);
        }).toList();

        List<CourseContentResponse.exercise> sortedExercises = exercises.stream()
                .sorted(Comparator.comparing(CourseContentResponse.exercise::dueDate).reversed()).toList();

        return new CourseContentResponse(courseName, sortedExercises);
    }

    private record ModuleWithDate(String id, String name, String url, LocalDateTime dueDate) {
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
            if (!detailHasDeadline(detail)) {
                return false;
            }
        }
        return true;
    }

    private boolean detailHasDeadline(CourseDetailResponse detail) {
        if (detail.moduleResponse() == null)
            return true;
        for (var module : detail.moduleResponse()) {
            if (module.dates() != null) {
                for (var date : module.dates()) {
                    if (ScheduleConstant.DUE_DATE_LABEL.equalsIgnoreCase(date.label())) {
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

        // Circuit breaker open or Moodle unavailable — fall back to date-based
        // inference only
        if (assignmentDetail == null) {
            log.debug("[Schedule Service] Moodle unavailable for assignmentId={}, inferring status from due date",
                    assignmentId);
            if (dueDate.isBefore(now)) {
                return DeadlineStatus.OVERDUE;
            }
            if (dueDate.isBefore(now.plusHours(ScheduleConstant.NEAR_DEADLINE_HOURS))) {
                return DeadlineStatus.NEARDEADLINE;
            }
            return DeadlineStatus.UPCOMING;
        }

        if (isSubmittedAssignment(assignmentDetail, assignmentId)) {
            return DeadlineStatus.DONE;
        }
        if (dueDate.isBefore(now)) {
            return DeadlineStatus.OVERDUE;
        }
        if (dueDate.isBefore(now.plusHours(ScheduleConstant.NEAR_DEADLINE_HOURS))) {
            return DeadlineStatus.NEARDEADLINE;
        }
        return DeadlineStatus.UPCOMING;

    }

    /**
     * Like {@link #determineDeadlineStatus(LocalDateTime, String, String)} but reads from a pre-fetched map. When the
     * map entry is null (circuit open or call failed), falls back to date-only inference so no additional HTTP call is
     * made.
     */
    private DeadlineStatus determineDeadlineStatusFromCache(LocalDateTime dueDate,
            AssignmentDetailResponse assignmentDetail) {
        LocalDateTime now = LocalDateTime.now();

        if (assignmentDetail == null) {
            log.debug("[Schedule Service] No submission data for dueDate={}, inferring from date only", dueDate);
            if (dueDate.isBefore(now)) {
                return DeadlineStatus.OVERDUE;
            }
            if (dueDate.isBefore(now.plusHours(ScheduleConstant.NEAR_DEADLINE_HOURS))) {
                return DeadlineStatus.NEARDEADLINE;
            }
            return DeadlineStatus.UPCOMING;
        }

        if (isSubmittedAssignment(assignmentDetail, null)) {
            return DeadlineStatus.DONE;
        }
        if (dueDate.isBefore(now)) {
            return DeadlineStatus.OVERDUE;
        }
        if (dueDate.isBefore(now.plusHours(ScheduleConstant.NEAR_DEADLINE_HOURS))) {
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

        return ScheduleConstant.SUBMITTED_STATUS.equalsIgnoreCase(assignmentDetail.lastAttempt().submission().status());
    }

    private List<IcsEvent> removeDuplicateSchedule(List<IcsEvent> icsEventList) {
        if (icsEventList == null || icsEventList.isEmpty()) {
            return List.of();
        }

        Map<String, IcsEvent> uniqueEventsByClassCode = new LinkedHashMap<>();
        for (IcsEvent icsEvent : icsEventList) {
            if (icsEvent == null || icsEvent.getClassCode() == null || icsEvent.getClassCode().isBlank()) {
                continue;
            }
            uniqueEventsByClassCode.putIfAbsent(icsEvent.getClassCode(), icsEvent);
        }

        return new ArrayList<>(uniqueEventsByClassCode.values());
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
        return dotCount >= ScheduleConstant.DOT_COUNT_FOR_LAB_CLASS;
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
                return Integer.valueOf(matcher.group(1));
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

    private List<CourseCalendarResponse.Course> sortCoursesByClassCode(List<CourseCalendarResponse.Course> courses) {
        return courses.stream().sorted(Comparator.comparing(CourseCalendarResponse.Course::courseCode,
                Comparator.nullsLast(Comparator.naturalOrder()))).toList();
    }

    /**
     * Syncs all Moodle deadlines for the active semester into the TemporaryDeadline table. Runs asynchronously after
     * signup to pre-populate the deadline cache without blocking the auth response.
     */
}
