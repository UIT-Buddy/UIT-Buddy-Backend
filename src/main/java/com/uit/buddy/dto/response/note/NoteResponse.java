package com.uit.buddy.dto.response.note;

import java.time.LocalDateTime;

public record NoteResponse(String mssv, String content, LocalDateTime updatedAt) {
}
