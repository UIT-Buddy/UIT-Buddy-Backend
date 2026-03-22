package com.uit.buddy.mapper.social;

import com.uit.buddy.dto.response.social.FriendRequestResponse;
import com.uit.buddy.dto.response.social.FriendshipResponse;
import com.uit.buddy.dto.response.social.UserSummary;
import com.uit.buddy.entity.social.FriendRequest;
import com.uit.buddy.entity.social.Friendship;
import com.uit.buddy.entity.user.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FriendMapper {

    UserSummary toUserSummary(Student student);

    FriendRequestResponse toFriendRequestResponse(FriendRequest friendRequest);

    @Mapping(target = "createdAt", source = "friendship.createdAt")
    FriendshipResponse toFriendshipResponse(Friendship friendship, Student friend);
}
