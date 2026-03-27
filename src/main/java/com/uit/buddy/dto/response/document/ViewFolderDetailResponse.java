package com.uit.buddy.dto.response.document;

import com.uit.buddy.enums.FileSizeUnit;
import com.uit.buddy.enums.FileType;
import java.util.List;
import java.util.UUID;

public record ViewFolderDetailResponse(UUID folderId, String folderName, String folderPath, UUID parentFolderId,
        List<FolderResponse> folders, List<FileResponse> files

) {
    public record FolderResponse(UUID folderId, String folderName, int folderItemCount // this is total of list of
                                                                                       // folders and files in the
                                                                                       // folder
    ) {
    };

    public record FileResponse(UUID fileId, String fileName, String fileUrl, float fileSize, FileSizeUnit fileSizeUnit,
            FileType fileType) {
    };
}
