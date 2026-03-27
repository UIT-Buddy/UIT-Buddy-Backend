package com.uit.buddy.dto.response.document;

import com.uit.buddy.enums.FileSizeUnit;
import com.uit.buddy.enums.FileType;
import java.util.UUID;

public record DocumentFileResponse(UUID fileId, String fileName, String fileUrl, UUID folderId, float fileSize,
        FileSizeUnit fileSizeUnit, FileType fileType) {
}
