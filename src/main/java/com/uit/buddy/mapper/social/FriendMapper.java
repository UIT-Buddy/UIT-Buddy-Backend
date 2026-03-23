package com.uit.buddy.mapper.social;

import com.uit.buddy.dto.response.social.FriendshipResponse;
import com.uit.buddy.dto.response.social.PendingFriendRequestResponse;
import com.uit.buddy.dto.response.social.SentFriendRequestResponse;
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

    PendingFriendRequestResponse toPendingRequestResponse(FriendRequest friendRequest);

    SentFriendRequestResponse toSentRequestResponse(FriendRequest friendRequest);

    @Mapping(target = "friend", source = "friend")
    @Mapping(target = "createdAt", source = "friendship.createdAt")
    @Mapping(target = "id", source = "friendship.id")
    FriendshipResponse toFriendshipResponse(Friendship friendship, Student friend);
}
