package com.uit.buddy.service.academic.impl;

import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.entity.academic.*;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.StudentClassStatus;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.exception.system.SystemErrorCode;
import com.uit.buddy.exception.system.SystemException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.repository.academic.*;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.academic.ScheduleService;
import com.uit.buddy.util.IcsParser;
import com.uit.buddy.util.IcsParser.IcsEvent;
import com.uit.buddy.util.IcsParser.ParseResult;
import java.time.LocalDate;
import java.util.*;
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
}
