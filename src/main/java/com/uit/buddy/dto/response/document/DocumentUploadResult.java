package com.uit.buddy.dto.response.document;

import com.uit.buddy.enums.FileType;

public record DocumentUploadResult(String fileUrl, float fileSize, FileType fileType) {
}
