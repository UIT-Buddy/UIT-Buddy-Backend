package com.uit.buddy.dto.request.grade;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadGradeRequest(@NotNull(message = "Grade PDF file is required") MultipartFile gradeFile) {
    public boolean isPdfFile() {
        String filename = gradeFile.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }
}
