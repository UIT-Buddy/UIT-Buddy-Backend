package com.uit.buddy.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateFolderRequest(
        @NotBlank(message = "Folder name must not be empty") @Size(max = 255, message = "Folder name must not exceed 255 characters") String folderName,
        UUID parentFolderId) {
}
