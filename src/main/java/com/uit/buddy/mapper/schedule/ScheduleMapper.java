package com.uit.buddy.mapper.schedule;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uit.buddy.constant.IcsConstants;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse.Course;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.CreateDeadlineResponse;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import com.uit.buddy.entity.learning.StudentTask;
import com.uit.buddy.enums.DeadlineStatus;
import com.uit.buddy.enums.TaskType;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    String PERSONAL_DEADLINE = "personal";
    String UNKNOWN_DEADLINE = "unknown";

    @Mapping(target = "courseCode", source = "subjectClass.course.courseCode")
    @Mapping(target = "classId", source = "subjectClass.classCode")
    @Mapping(target = "labOfClassId", expression = "java(extractLabOfClassId(studentClass))")
    @Mapping(target = "isBlendedLearning", expression = "java(isBlendedLearningClass(studentClass.getSubjectClass().getClassType()))")
    @Mapping(target = "courseName", source = "subjectClass.course.courseName")
    @Mapping(target = "dayOfWeek", source = "subjectClass.dayOfWeek")
    @Mapping(target = "startTime", source = "subjectClass.startTime")
    @Mapping(target = "endTime", source = "subjectClass.endTime")
    @Mapping(target = "roomCode", source = "subjectClass.roomCode")
    @Mapping(target = "startPeriod", source = "subjectClass.startLesson")
    @Mapping(target = "endPeriod", source = "subjectClass.endLesson")
    @Mapping(target = "startDate", source = "subjectClass.startDate")
    @Mapping(target = "endDate", source = "subjectClass.endDate")
    @Mapping(target = "lecturer", source = "subjectClass.teacherName")
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

    default CourseContentResponse.exercise toExercise(StudentTask studentTask) {
        boolean isPersonal = studentTask.getTaskType() == TaskType.PERSONAL;
        return new CourseContentResponse.exercise(studentTask.getPersonalTitle(), studentTask.getReminderAt(), null,
                mapDeadlineStatus(studentTask), isPersonal);
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
        return studentClasses.stream().map(studentClass -> {
            Course mappedCourse = toCourse(studentClass);
            CourseContentResponse deadline = findMatchingDeadline(mappedCourse, courseDeadlines);
            return new Course(mappedCourse.courseCode(), mappedCourse.classId(), mappedCourse.courseName(),
                    mappedCourse.lecturer(), mappedCourse.dayOfWeek(), mappedCourse.startTime(),
                    mappedCourse.labOfClassId(), mappedCourse.isBlendedLearning(), mappedCourse.endTime(),
                    mappedCourse.startPeriod(), mappedCourse.endPeriod(), mappedCourse.roomCode(),
                    mappedCourse.startDate(), mappedCourse.endDate(), mappedCourse.credits(), deadline);
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

}
