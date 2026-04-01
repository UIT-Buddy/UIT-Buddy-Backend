package com.uit.buddy.dto.response.document;

import java.util.UUID;

public record UpdateFolderResponse(UUID folderId, String folderName, String folderPath) {
}