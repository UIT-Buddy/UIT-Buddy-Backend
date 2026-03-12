package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDetailResponse(
        UUID id,
        String title,
        String content,
        List<MediaResponse> medias,
        AuthorInfo author,
        Long likeCount,
        Long shareCount,
        Long commentCount,
        boolean isLiked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
