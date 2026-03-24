package com.uit.buddy.mapper.schedule;

import com.uit.buddy.dto.response.schedule.CourseCalendarResponse.Course;
import com.uit.buddy.entity.academic.StudentSubjectClass;
import java.time.LocalTime;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "courseCode", source = "subjectClass.classCode")
    @Mapping(target = "courseName", source = "subjectClass.course.courseName")
    @Mapping(target = "dayOfWeek", source = "subjectClass.dayOfWeek")
    @Mapping(target = "startTime", source = "subjectClass.startTime")
    @Mapping(target = "endTime", source = "subjectClass.endTime")
    @Mapping(target = "roomCode", source = "subjectClass.roomCode")
    @Mapping(target = "startLesson", source = "subjectClass.startLesson")
    @Mapping(target = "endLesson", source = "subjectClass.endLesson")
    @Mapping(target = "startDate", source = "subjectClass.startDate")
    @Mapping(target = "endDate", source = "subjectClass.endDate")
    Course toCourse(StudentSubjectClass studentClass);

    List<Course> toListCourse(List<StudentSubjectClass> studentClasses);

    default String mapStringToLocalTime(LocalTime value) {
        return value == null ? null : value.toString();
    }

}
