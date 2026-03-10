package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostFeedResponse(
                UUID id,
                String title,
                String contentSnippet,
                String imageUrl,
                String videoUrl,
                AuthorInfo author,
                Long likeCount,
                Long commentCount,
                Long shareCount,
                boolean isLiked,
                boolean isDisliked,
                boolean isShared,
                LocalDateTime createdAt) {
        public record AuthorInfo(
                        String homeClassCode,
                        String fullName,
                        String avatarUrl) {
        }
}