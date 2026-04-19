package com.uit.buddy.mapper.academic;

import com.uit.buddy.dto.response.academic.GradeResponse;
import com.uit.buddy.entity.academic.Grade;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    GradeResponse toResponse(Grade grade);
}
