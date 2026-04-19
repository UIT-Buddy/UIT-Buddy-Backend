package com.uit.buddy.dto.response.note;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteSummaryResponse(UUID id, UUID nodeId, String title, LocalDateTime updatedAt,
        LocalDateTime createdAt) {
}
