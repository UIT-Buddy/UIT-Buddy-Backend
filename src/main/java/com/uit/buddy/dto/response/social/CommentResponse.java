package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        String content,
        AuthorInfo author,
        List<CommentResponse> replies,
        LocalDateTime createdAt) {
    public record AuthorInfo(
            String mssv,
            String fullName,
            String avatarUrl) {
    }
}
