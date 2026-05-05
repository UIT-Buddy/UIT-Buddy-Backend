package com.uit.buddy.dto.request.note;

import jakarta.validation.constraints.NotNull;

public record UpsertNoteRequest(@NotNull(message = "Content cannot be null") String content) {
}
