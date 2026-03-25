package com.uit.buddy.mapper.user;

import com.uit.buddy.dto.response.user.FoundUserResponse;
import com.uit.buddy.dto.response.user.UserResponse;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FriendStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "friendStatus", source = "friendStatus")
    UserResponse toUserResponse(Student student, FriendStatus friendStatus);

    @Mapping(target = "friendStatus", source = "friendStatus")
    FoundUserResponse toFoundUserResponse(Student student, FriendStatus friendStatus);
}
