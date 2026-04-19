package com.uit.buddy.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateNoteRequest(
        @NotBlank(message = "Title is required") @Size(max = 255, message = "Title must not exceed 255 characters") String title,
        String content,
        UUID nodeId) {
}
