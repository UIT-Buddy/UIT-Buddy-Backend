package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostFeedResponse(
    UUID id,
    String title,
    String contentSnippet,
    List<MediaResponse> medias,
    AuthorInfo author,
    Long likeCount,
    Long commentCount,
    Long shareCount,
    boolean isLiked,
    LocalDateTime createdAt) {}
