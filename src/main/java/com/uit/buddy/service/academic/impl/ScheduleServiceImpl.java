package com.uit.buddy.service.academic.impl;

import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.entity.academic.*;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.ClassType;
import com.uit.buddy.enums.StudentClassStatus;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.repository.academic.*;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.util.IcsParser;
import com.uit.buddy.util.IcsParser.IcsEvent;
import com.uit.buddy.util.IcsParser.ParseResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final IcsParser icsParser;
    private final StudentRepository studentRepository;
    private final SubjectClassRepository subjectClassRepository;
    private final StudentSubjectClassRepository studentSubjectClassRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;

    @Override
    @Transactional
    public void uploadSchedule(String mssv, UploadScheduleRequest request) {
        log.info("[Schedule Service] Uploading schedule for student: {}", mssv);

        // Validate file type
        validateIcsFile(request.icsFile());

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new UserException(UserErrorCode.STUDENT_NOT_FOUND));

        try {
            ParseResult result = icsParser.parseIcsFile(request.icsFile().getInputStream());

            if (result.getStudentId() != null && !result.getStudentId().equals(mssv)) {
                log.error("[Schedule Service] MSSV mismatch! File: {}, Account: {}", result.getStudentId(), mssv);
                throw new ScheduleException(ScheduleErrorCode.INVALID_OWNER);
            }

            List<IcsEvent> events = result.getEvents();
            log.info("[Schedule Service] Parsed {} events for student {}", events.size(), result.getStudentId());

            for (IcsParser.IcsEvent event : events) {
                processEvent(student, event);
            }
        } catch (ScheduleException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Schedule Service] Failed to process ICS for student: {}", mssv, e);
            throw new RuntimeException("Failed to process schedule file", e);
        }
    }

    private void processEvent(Student student, IcsEvent event) {
        SubjectClass subjectClass = createOrUpdateSubjectClass(event);
        createStudentSubjectClass(student, subjectClass);
    }

    private SubjectClass createOrUpdateSubjectClass(IcsEvent event) {
        return subjectClassRepository.findById(event.getClassCode()).orElseGet(() -> {
            // Extract course code from class code (e.g., PE232.Q219 -> PE232)
            String courseCode = extractCourseCode(event.getClassCode());
            String semesterCode = getCurrentSemesterCode();
            ClassType classType = convertFrequencyToClassType(event.getFrequency());

            SubjectClass newClass = SubjectClass.builder().classCode(event.getClassCode())
                    .teacherName(event.getTeacherName()).dayOfWeek(event.getDayOfWeek()).startTime(event.getStartTime())
                    .endTime(event.getEndTime()).startLesson(event.getStartLesson()).endLesson(event.getEndLesson())
                    .roomCode(event.getRoomCode()).interval(event.getInterval() != null ? event.getInterval() : 1)
                    .classType(classType).build();

            setCourseAndSemester(newClass, courseCode, semesterCode);

            return subjectClassRepository.save(newClass);
        });
    }

    private String extractCourseCode(String classCode) {
        // Extract course code from class code (e.g., PE232.Q219 -> PE232)
        if (classCode != null && classCode.contains(".")) {
            return classCode.substring(0, classCode.indexOf("."));
        }
        return classCode;
    }

    private String getCurrentSemesterCode() {
        LocalDate currentDate = LocalDate.now();
        Optional<Semester> currentSemester = semesterRepository.findCurrentSemester(currentDate);

        if (currentSemester.isPresent()) {
            return currentSemester.get().getSemesterCode();
        }

        int currentYear = currentDate.getYear();
        int semesterNumber = currentDate.getMonthValue() <= 6 ? 1 : 2;
        String fallbackSemesterCode = currentYear + "." + semesterNumber;

        log.warn("[Schedule Service] No current semester found, using fallback: {}", fallbackSemesterCode);
        return fallbackSemesterCode;
    }

    private ClassType convertFrequencyToClassType(String frequency) {
        if (frequency == null) {
            return ClassType.WEEKLY;
        }

        return switch (frequency.toUpperCase()) {
        case "WEEKLY" -> ClassType.WEEKLY;
        default -> ClassType.WEEKLY;
        };
    }

    private void setCourseAndSemester(SubjectClass subjectClass, String courseCode, String semesterCode) {
        Course course = courseRepository.findById(courseCode).orElseThrow(() -> {
            log.error("[Schedule Service] Course not found: {}", courseCode);
            return new ScheduleException(ScheduleErrorCode.COURSE_NOT_FOUND);
        });

        Semester semester = semesterRepository.findById(semesterCode).orElseThrow(() -> {
            log.error("[Schedule Service] Semester not found: {}", semesterCode);
            return new ScheduleException(ScheduleErrorCode.SEMESTER_NOT_FOUND);
        });

        subjectClass.setCourse(course);
        subjectClass.setSemester(semester);

        log.info("[Schedule Service] Linked class {} to course {} and semester {}", subjectClass.getClassCode(),
                courseCode, semesterCode);
    }

    private void createStudentSubjectClass(Student student, SubjectClass subjectClass) {
        boolean exists = studentSubjectClassRepository.existsByStudentMssvAndSubjectClassClassCode(student.getMssv(),
                subjectClass.getClassCode());

        if (!exists) {
            StudentSubjectClass studentSubjectClass = StudentSubjectClass.builder().student(student)
                    .subjectClass(subjectClass).status(StudentClassStatus.STUDYING).build();

            studentSubjectClassRepository.save(studentSubjectClass);
        }
    }

    private void validateIcsFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".ics")) {
            log.error("[Schedule Service] Invalid file type: {}", originalFilename);
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("text/calendar")
                && !contentType.equals("application/octet-stream")) {
            log.error("[Schedule Service] Invalid content type: {}", contentType);
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }
    }
}
