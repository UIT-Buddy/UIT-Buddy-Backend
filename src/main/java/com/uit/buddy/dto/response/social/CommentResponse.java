package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(UUID id, String content, UserSummary user, Long likeCount, Long replyCount,
        boolean isLiked, LocalDateTime createdAt, LocalDateTime updatedAt, UUID parentId) {
}
