package com.uit.buddy.dto.response.social;

public record FoundPostResponse(
        AuthorInfo author,
        String title,
        String content,
        String imageUrl,
        String videoUrl
) {
    public record AuthorInfo(
            String mssv,
            String fullName,
            String avatarUrl) {
    }
}
