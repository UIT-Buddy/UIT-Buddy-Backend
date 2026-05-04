package com.uit.buddy.mapper.schedule;

import com.uit.buddy.constant.IcsConstants;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse.Course;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.CreateDeadlineResponse;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import com.uit.buddy.entity.learning.StudentTask;
import com.uit.buddy.enums.DeadlineStatus;
import com.uit.buddy.enums.TaskType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    String PERSONAL_DEADLINE = "personal";
    String UNKNOWN_DEADLINE = "unknown";

    @Mapping(target = "courseCode", expression = "java(safeCourseCode(studentClass))")
    @Mapping(target = "classId", expression = "java(safeClassCode(studentClass))")
    @Mapping(target = "labOfClassId", expression = "java(extractLabOfClassId(studentClass))")
    @Mapping(target = "isBlendedLearning", expression = "java(isBlendedLearningClass(safeClassType(studentClass)))")
    @Mapping(target = "courseName", expression = "java(safeCourseName(studentClass))")
    @Mapping(target = "dayOfWeek", expression = "java(safeDayOfWeek(studentClass))")
    @Mapping(target = "startTime", expression = "java(safeStartTime(studentClass))")
    @Mapping(target = "endTime", expression = "java(safeEndTime(studentClass))")
    @Mapping(target = "roomCode", expression = "java(safeRoomCode(studentClass))")
    @Mapping(target = "startPeriod", expression = "java(safeStartLesson(studentClass))")
    @Mapping(target = "endPeriod", expression = "java(safeEndLesson(studentClass))")
    @Mapping(target = "startDate", expression = "java(safeStartDate(studentClass))")
    @Mapping(target = "endDate", expression = "java(safeEndDate(studentClass))")
    @Mapping(target = "lecturer", expression = "java(safeTeacherName(studentClass))")
    @Mapping(target = "credits", constant = "0")
    @Mapping(target = "deadline", ignore = true)
    Course toCourse(StudentSubjectClass studentClass);

    @Mapping(target = "classCode", source = "classCode")
    @Mapping(target = "isPersonal", expression = "java(studentTask.getTaskType() == com.uit.buddy.enums.TaskType.PERSONAL)")
    @Mapping(target = "deadlineName", source = "personalTitle")
    @Mapping(target = "dueDate", source = "reminderAt")
    @Mapping(target = "status", expression = "java(mapDeadlineStatus(studentTask))")
    CreateDeadlineResponse toCreateDeadlineResponse(StudentTask studentTask);

    @Mapping(target = "courseName", expression = "java(resolveCourseName(studentTask))")
    @Mapping(target = "exercises", expression = "java(java.util.List.of(toExercise(studentTask)))")
    CourseContentResponse toCourseContentResponse(StudentTask studentTask);

    List<Course> toListCourse(List<StudentSubjectClass> studentClasses);

    default DeadlineStatus mapDeadlineStatus(StudentTask studentTask) {
        if (studentTask == null) {
            return DeadlineStatus.UPCOMING;
        }

        if (Boolean.TRUE.equals(studentTask.getIsCompleted())) {
            return DeadlineStatus.DONE;
        }

        LocalDateTime dueDate = studentTask.getReminderAt();
        if (dueDate == null) {
            return DeadlineStatus.UPCOMING;
        }

        LocalDateTime now = LocalDateTime.now();
        if (dueDate.isBefore(now)) {
            return DeadlineStatus.OVERDUE;
        }

        if (dueDate.isBefore(now.plusDays(1))) {
            return DeadlineStatus.NEARDEADLINE;
        }

        return DeadlineStatus.UPCOMING;
    }

    default CourseContentResponse.Exercise toExercise(StudentTask studentTask) {
        boolean isPersonal = studentTask.getTaskType() == TaskType.PERSONAL;
        return new CourseContentResponse.Exercise(studentTask.getId(), studentTask.getPersonalTitle(),
                studentTask.getReminderAt(), null, mapDeadlineStatus(studentTask), isPersonal);
    }

    default String resolveCourseName(StudentTask studentTask) {
        boolean isPersonal = studentTask.getTaskType() == TaskType.PERSONAL;
        if (isPersonal) {
            return PERSONAL_DEADLINE;
        }

        if (studentTask.getClassCode() != null) {
            return studentTask.getClassCode();
        }

        return UNKNOWN_DEADLINE;
    }

    default List<Course> toListCourseWithDeadlines(List<StudentSubjectClass> studentClasses,
            List<CourseContentResponse> courseDeadlines) {
        return studentClasses.stream().map(ssc -> {
            // Construct Course fields directly using safe accessors to avoid MapStruct
            // generating nested proxy traversal (course → course.courseCode) that triggers
            // LazyInitializationException outside a Hibernate session.
            Course rawCourse = toCourse(ssc);
            CourseContentResponse deadline = findMatchingDeadline(rawCourse, courseDeadlines);
            return new Course(safeCourseCode(ssc), safeClassCode(ssc), safeCourseName(ssc), safeTeacherName(ssc),
                    safeDayOfWeek(ssc), safeStartTime(ssc), extractLabOfClassId(ssc),
                    isBlendedLearningClass(safeClassType(ssc)), safeEndTime(ssc), safeStartLesson(ssc),
                    safeEndLesson(ssc), safeRoomCode(ssc), safeStartDate(ssc), safeEndDate(ssc),
                    ssc.getCredits() != null ? ssc.getCredits() : 0, deadline);
        }).toList();
    }

    default String extractLabOfClassId(StudentSubjectClass studentClass) {
        if (studentClass == null || studentClass.getSubjectClass() == null) {
            return null;
        }

        String classId = studentClass.getSubjectClass().getClassCode();
        if (classId == null || classId.isBlank()) {
            return null;
        }

        int firstDot = classId.indexOf('.');
        if (firstDot < 0) {
            return null;
        }

        int secondDot = classId.indexOf('.', firstDot + 1);
        if (secondDot < 0) {
            return null;
        }

        return classId.substring(0, secondDot);
    }

    default boolean isBlendedLearningClass(String classType) {
        return IcsConstants.BLENDED_LEARNING.equalsIgnoreCase(classType);
    }

    default CourseContentResponse findMatchingDeadline(Course course, List<CourseContentResponse> courseDeadlines) {
        if (courseDeadlines == null || courseDeadlines.isEmpty()) {
            return null;
        }

        String normalizedCode = normalize(course.courseCode());
        String normalizedName = normalize(course.courseName());

        for (CourseContentResponse content : courseDeadlines) {
            String normalizedDeadlineCourse = normalize(content.courseName());

            if (normalizedCode != null && normalizedDeadlineCourse.contains(normalizedCode)) {
                return content;
            }

            if (normalizedName != null && (normalizedDeadlineCourse.contains(normalizedName)
                    || normalizedName.contains(normalizedDeadlineCourse))) {
                return content;
            }
        }

        return null;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    default String mapStringToLocalTime(LocalTime value) {
        return value == null ? null : value.toString();
    }

    // ─── Safe accessors (avoid LazyInitializationException on lazy proxies) ────

    default String safeCourseCode(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null || ssc.getSubjectClass().getCourse() == null) {
            return null;
        }
        return ssc.getSubjectClass().getCourse().getCourseCode();
    }

    default String safeClassCode(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        return ssc.getSubjectClass().getClassCode();
    }

    default String safeCourseName(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null || ssc.getSubjectClass().getCourse() == null) {
            return null;
        }
        return ssc.getSubjectClass().getCourse().getCourseName();
    }

    default String safeClassType(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        return ssc.getSubjectClass().getClassType();
    }

    default Integer safeDayOfWeek(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        return ssc.getSubjectClass().getDayOfWeek();
    }

    default String safeStartTime(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        LocalTime t = ssc.getSubjectClass().getStartTime();
        return t == null ? null : t.toString();
    }

    default String safeEndTime(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        LocalTime t = ssc.getSubjectClass().getEndTime();
        return t == null ? null : t.toString();
    }

    default String safeRoomCode(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        return ssc.getSubjectClass().getRoomCode();
    }

    default String safeStartLesson(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        Integer l = ssc.getSubjectClass().getStartLesson();
        return l == null ? null : l.toString();
    }

    default String safeEndLesson(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        Integer l = ssc.getSubjectClass().getEndLesson();
        return l == null ? null : l.toString();
    }

    default String safeStartDate(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        java.time.LocalDate d = ssc.getSubjectClass().getStartDate();
        return d == null ? null : d.toString();
    }

    default String safeEndDate(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        java.time.LocalDate d = ssc.getSubjectClass().getEndDate();
        return d == null ? null : d.toString();
    }

    default String safeTeacherName(StudentSubjectClass ssc) {
        if (ssc == null || ssc.getSubjectClass() == null) {
            return null;
        }
        return ssc.getSubjectClass().getTeacherName();
    }

}
