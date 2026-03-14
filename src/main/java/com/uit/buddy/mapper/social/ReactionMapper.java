package com.uit.buddy.mapper.social;

import com.uit.buddy.dto.response.social.UserReactionResponse;
import com.uit.buddy.repository.social.projection.ReactionProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReactionMapper {

    @Mapping(target = "user.mssv", source = "mssv")
    @Mapping(target = "user.fullName", source = "fullName")
    @Mapping(target = "user.avatarUrl", source = "avatarUrl")
    @Mapping(target = "reactedAt", source = "reactedAt")
    UserReactionResponse toReactionResponse(ReactionProjection p);
}
