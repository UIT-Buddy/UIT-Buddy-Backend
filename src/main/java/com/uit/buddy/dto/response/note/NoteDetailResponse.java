package com.uit.buddy.dto.response.note;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteDetailResponse(UUID id, UUID nodeId, String title, String content, LocalDateTime updatedAt,
        LocalDateTime createdAt) {
}
