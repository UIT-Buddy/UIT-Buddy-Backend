package com.uit.buddy.dto.request.note;

import java.util.UUID;

public record SaveNoteToDocumentRequest(
        String fileName,
        UUID folderId) {
}
