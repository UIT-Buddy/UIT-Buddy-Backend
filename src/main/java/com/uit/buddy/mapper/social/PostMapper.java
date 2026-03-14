package com.uit.buddy.mapper.social;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.dto.response.social.AuthorInfo;
import com.uit.buddy.dto.response.social.MediaResponse;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.repository.social.projection.PostFeedProjection;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.util.TextUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { TextUtils.class,
        Collections.class })
public abstract class PostMapper {

    protected ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public abstract AuthorInfo toAuthorInfo(Student student);

    @Mapping(target = "mssv", source = "authorMssv")
    @Mapping(target = "fullName", source = "authorFullName")
    @Mapping(target = "avatarUrl", source = "authorAvatarUrl")
    @Mapping(target = "homeClassCode", source = "authorHomeClassCode")
    public abstract AuthorInfo toAuthorFromProjection(PostFeedProjection projection);

    @Mapping(target = "contentSnippet", expression = "java(TextUtils.truncate(p.getContent()))")
    @Mapping(target = "author", source = "p")
    @Mapping(target = "medias", expression = "java(mapMedias(p.getMedias()))")
    @Mapping(target = "isLiked", source = "isLiked")
    public abstract PostFeedResponse toPostFeedResponse(PostFeedProjection p);

    @Mapping(target = "author", source = "p")
    @Mapping(target = "medias", expression = "java(mapMedias(p.getMedias()))")
    @Mapping(target = "isLiked", source = "isLiked")
    public abstract PostDetailResponse toPostDetailResponseFromProjection(PostFeedProjection p);

    public abstract PostDetailResponse toPostDetailResponse(Post post);

    protected List<MediaResponse> mapMedias(String mediasJson) {
        if (mediasJson == null || mediasJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(mediasJson, new TypeReference<List<MediaResponse>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
