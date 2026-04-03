package com.uit.buddy.dto.response.document;

import com.uit.buddy.enums.FileSizeUnit;
import com.uit.buddy.enums.FileType;
import java.util.List;
import java.util.UUID;

public record ViewFolderDetailResponse(UUID folderId, String folderName, String folderPath, UUID parentFolderId,
        List<FolderResponse> folders, List<FileResponse> files, PaginationMeta paging

) {
    public record FolderResponse(UUID folderId, String folderName, int folderItemCount) {
    };

    public record FileResponse(UUID fileId, String fileName, String fileUrl, float fileSize, FileSizeUnit fileSizeUnit,
            FileType fileType) {
    };

    public record PaginationMeta(int page, int limit, long total, int totalPages) {
    };
}
