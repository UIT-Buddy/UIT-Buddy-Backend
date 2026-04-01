package com.uit.buddy.dto.request.schedule;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record UploadScheduleRequest(@NotNull(message = "ICS file is required") MultipartFile icsFile) {
    public boolean isIcsFile() {
        String filename = icsFile.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".ics");
    }
}
