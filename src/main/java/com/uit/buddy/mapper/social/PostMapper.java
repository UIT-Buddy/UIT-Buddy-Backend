package com.uit.buddy.mapper.social;

import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.dto.response.social.PostResponse;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PostMapper {

    @Mapping(source = "author.mssv", target = "mssv")
    @Mapping(source = "author.fullName", target = "authorName")
    @Mapping(source = "author.avatarUrl", target = "authorAvatar")
    PostResponse toPostResponse(Post post);

    @Mapping(source = "content", target = "contentSnippet")
    
    PostFeedResponse toPostFeedResponse(Post post);

    PostDetailResponse toPostDetailResponse(Post post);

    PostFeedResponse.AuthorInfo toFeedAuthor(Student student);

    PostDetailResponse.AuthorInfo toDetailAuthor(Student student);

}
