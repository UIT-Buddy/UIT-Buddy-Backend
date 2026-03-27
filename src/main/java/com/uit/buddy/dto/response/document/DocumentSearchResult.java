package com.uit.buddy.dto.response.document;

import java.util.UUID;

public record DocumentSearchResult(String fileName, String fileLocation, UUID parentFolderId, UUID documentId,
        String url) {
}
