package com.uit.buddy.mapper.social;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.dto.response.social.AuthorInfo;
import com.uit.buddy.dto.response.social.MediaResponse;
import com.uit.buddy.dto.response.social.PostDetailResponse;
import com.uit.buddy.dto.response.social.PostFeedResponse;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.repository.social.PostRepository;
import com.uit.buddy.util.TextUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, imports = { TextUtils.class,
        Collections.class })
public interface PostMapper {

    ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    AuthorInfo toAuthorInfo(Student student);

    @Mapping(target = "mssv", source = "authorMssv")
    @Mapping(target = "fullName", source = "authorFullName")
    @Mapping(target = "avatarUrl", source = "authorAvatarUrl")
    @Mapping(target = "homeClassCode", source = "authorHomeClassCode")
    AuthorInfo toAuthorFromProjection(PostRepository.PostFeedProjection projection);

    @Mapping(target = "contentSnippet", expression = "java(TextUtils.truncate(p.getContent()))")
    @Mapping(target = "author", source = "p")
    @Mapping(target = "medias", expression = "java(mapMedias(p.getMedias()))")
    PostFeedResponse toPostFeedResponse(PostRepository.PostFeedProjection p);

    @Mapping(target = "author", source = "p")
    @Mapping(target = "medias", expression = "java(mapMedias(p.getMedias()))")
    PostDetailResponse toPostDetailResponseFromProjection(PostRepository.PostFeedProjection p);

    default List<MediaResponse> mapMedias(String mediasJson) {
        if (mediasJson == null || mediasJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(mediasJson, new TypeReference<List<MediaResponse>>() {
            });
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}