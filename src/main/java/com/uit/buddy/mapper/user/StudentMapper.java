package com.uit.buddy.mapper.user;

import com.uit.buddy.dto.response.auth.StudentResponse;
import com.uit.buddy.entity.user.Student;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper {

    StudentResponse toStudentResponse(Student student);

}