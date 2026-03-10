package com.uit.buddy.mapper.social;

import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.util.TextUtils;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = TextUtils.class)
public interface PostMapper {

    @Mapping(target = "contentSnippet", expression = "java(TextUtils.truncate(post.getContent()))")
    PostFeedResponse toPostFeedResponse(Post post);

    PostDetailResponse toPostDetailResponse(Post post);

    PostFeedResponse.AuthorInfo toFeedAuthor(Student student);

    PostDetailResponse.AuthorInfo toDetailAuthor(Student student);

}
