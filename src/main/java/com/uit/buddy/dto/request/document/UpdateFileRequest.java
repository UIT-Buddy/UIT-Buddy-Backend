package com.uit.buddy.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record UpdateFileRequest(@NotBlank(message = "File name is required") String fileName, UUID folderId) {
}
