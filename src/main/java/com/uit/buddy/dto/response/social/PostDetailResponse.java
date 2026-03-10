package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostDetailResponse(
                UUID id,
                String title,
                String content,
                String imageUrl,
                String videoUrl,
                AuthorInfo author,
                Long likeCount,
                Long commentCount,
                Long shareCount,
                boolean isLiked,
                boolean isDisliked,
                boolean isShared,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
        public record AuthorInfo(
                        String homeClassCode,
                        String fullName,
                        String avatarUrl) {
        }
}
