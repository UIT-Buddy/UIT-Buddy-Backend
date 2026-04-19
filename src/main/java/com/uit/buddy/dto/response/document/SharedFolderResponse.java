package com.uit.buddy.dto.response.document;

import com.uit.buddy.dto.response.auth.StudentResponse;
import com.uit.buddy.enums.AccessRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record SharedFolderResponse(UUID folderId, String folderName, String folderPath, UUID parentFolderId,
        int folderItemCount, StudentResponse owner, AccessRole accessRole, LocalDateTime sharedAt) {
}
