package com.uit.buddy.dto.request.document;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public record CreateFileRequest(@Schema(description = "Document files", format = "binary") List<MultipartFile> files,
        UUID folderId) {
}
