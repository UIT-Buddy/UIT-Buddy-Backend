package com.uit.buddy.mapper.schedule;

import java.time.LocalTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.uit.buddy.dto.response.schedule.CourseCalendarResponse.Course;
import com.uit.buddy.entity.academic.StudentSubjectClass;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "courseCode", source = "subjectClass.classCode")
    @Mapping(target = "courseName", source = "subjectClass.course.courseName")
    @Mapping(target = "dayOfWeek", source = "subjectClass.dayOfWeek")
    @Mapping(target = "startTime", source = "subjectClass.startTime")
    @Mapping(target = "endTime", source = "subjectClass.endTime")
    Course toCourse(StudentSubjectClass studentClass);

    List<Course> toListCourse(List<StudentSubjectClass> studentClasses);

    default String mapStringToLocalTime(LocalTime value) {
        return value == null ? null : value.toString();
    }

}
