package com.uit.buddy.mapper.schedule;

import com.uit.buddy.dto.response.schedule.ScheduleResponse;
import com.uit.buddy.entity.academic.SubjectClass;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScheduleMapper {

    @Mapping(target = "courseName", source = "course.courseName")
    ScheduleResponse toScheduleResponse(SubjectClass subjectClass);
}
