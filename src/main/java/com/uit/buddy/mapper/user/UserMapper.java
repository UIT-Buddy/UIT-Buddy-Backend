package com.uit.buddy.mapper.user;

import com.uit.buddy.dto.response.user.FoundUserResponse;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.entity.user.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "isFriend", source = "isFriend")
    UserResponse toUserResponse(Student student, boolean isFriend);

    @Mapping(target = "isFriend", source = "isFriend")
    FoundUserResponse toFoundUserResponse(Student student, boolean isFriend);
}
