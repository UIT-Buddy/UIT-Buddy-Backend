package com.uit.buddy.mapper.schedule;

import com.uit.buddy.constant.IcsConstants;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse.Course;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

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
    @Mapping(target = "credits", expression = "java(studentClass.getCredits())")
    @Mapping(target = "deadline", ignore = true)
    Course toCourse(StudentSubjectClass studentClass);

    List<Course> toListCourse(List<StudentSubjectClass> studentClasses);

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
