package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PostDetailResponse(
        UUID id,
        String title,
        String content,
        String imageUrl,
        String videoUrl,
        AuthorInfo author,
        Long likeCount,
        Long dislikeCount,
        Long commentCount,
        List<CommentResponse> comments,
        List<ReactionUserInfo> likedBy,
        List<ReactionUserInfo> dislikedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
    public record AuthorInfo(
            String mssv,
            String fullName,
            String avatarUrl) {
    }

    public record ReactionUserInfo(
            String mssv,
            String fullName,
            String avatarUrl) {
    }
}
