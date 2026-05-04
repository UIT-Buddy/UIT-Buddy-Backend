package com.uit.buddy.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateFolderRequest(@NotBlank(message = "Folder name is required") String folderName,
        UUID parentFolderId) {
}