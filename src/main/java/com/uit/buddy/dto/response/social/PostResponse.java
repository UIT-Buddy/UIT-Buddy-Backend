package com.uit.buddy.dto.response.social;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostResponse(
        UUID id,
        String mssv,
        String authorName,
        String authorAvatar,
        String title,
        String content,
        String imageUrl,
        String videoUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
