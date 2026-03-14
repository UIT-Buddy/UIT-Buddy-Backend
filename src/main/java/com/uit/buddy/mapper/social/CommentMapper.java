package com.uit.buddy.mapper.social;

import com.uit.buddy.dto.response.social.CommentResponse;
import com.uit.buddy.repository.social.projection.CommentProjection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {

  @Mapping(target = "user.mssv", source = "mssv")
  @Mapping(target = "user.fullName", source = "fullName")
  @Mapping(target = "user.avatarUrl", source = "avatarUrl")
  CommentResponse toCommentResponse(CommentProjection p);
}
