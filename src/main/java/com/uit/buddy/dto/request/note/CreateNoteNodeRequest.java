package com.uit.buddy.dto.request.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateNoteNodeRequest(
        @NotBlank(message = "Node name is required") @Size(max = 120, message = "Node name must not exceed 120 characters") String name,
        UUID parentId) {
}
