package com.uit.buddy.mapper.social;

import com.uit.buddy.dto.response.social.UserShareResponse;
import com.uit.buddy.repository.social.projection.ShareProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShareMapper {

    @Mapping(target = "user.mssv", source = "mssv")
    @Mapping(target = "user.fullName", source = "fullName")
    @Mapping(target = "user.avatarUrl", source = "avatarUrl")
    @Mapping(target = "sharedAt", source = "sharedAt")
    UserShareResponse toShareResponse(ShareProjection p);
}
