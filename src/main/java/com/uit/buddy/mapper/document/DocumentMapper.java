package com.uit.buddy.mapper.document;

import com.uit.buddy.constant.StorageConstants;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse.FileResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse.FolderResponse;
import com.uit.buddy.entity.document.Document;
import com.uit.buddy.entity.document.Folder;
import com.uit.buddy.enums.FileSizeUnit;
import com.uit.buddy.enums.FileType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import com.uit.buddy.dto.response.document.UpdateFolderResponse;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DocumentMapper {

    @Mapping(target = "fileId", source = "id")
    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "fileUrl", source = "fileUrl")
    @Mapping(target = "folderId", source = "folderId")
    @Mapping(target = "fileSize", expression = "java(toDisplaySizeValue(document.getFileSize()))")
    @Mapping(target = "fileSizeUnit", expression = "java(toDisplaySizeUnit(document.getFileSize()))")
    @Mapping(target = "fileType", expression = "java(mapFileType(document.getFileType()))")
    DocumentFileResponse toDocumentFileResponse(Document document);

    @Mapping(target = "fileId", source = "id")
    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "fileUrl", source = "fileUrl")
    @Mapping(target = "fileSize", expression = "java(toDisplaySizeValue(document.getFileSize()))")
    @Mapping(target = "fileSizeUnit", expression = "java(toDisplaySizeUnit(document.getFileSize()))")
    @Mapping(target = "fileType", expression = "java(mapFileType(document.getFileType()))")
    FileResponse toFileResponse(Document document);

    @Mapping(target = "folderId", source = "id")
    @Mapping(target = "folderName", source = "folderName")
    @Mapping(target = "folderItemCount", expression = "java(folder.getChildren().size() + folder.getFiles().size())")
    FolderResponse toFolderResponse(Folder folder);

    @Mapping(target = "fileName", source = "fileName")
    @Mapping(target = "fileLocation", expression = "java(buildFileLocation(document))")
    @Mapping(target = "parentFolderId", source = "folderId")
    @Mapping(target = "documentId", source = "id")
    @Mapping(target = "url", source = "fileUrl")
    DocumentSearchResult toSearchResult(Document document);

    @Mapping(target = "folderId", source = "id")
    @Mapping(target = "folderName", source = "folderName")
    @Mapping(target = "folderPath", expression = "java(buildFolderPath(folder))")
    UpdateFolderResponse toUpdateFolderResponse(Folder folder);

    default float toDisplaySizeValue(Float sizeInMb) {
        float safeSizeInMb = sizeInMb == null ? 0f : sizeInMb;
        if (safeSizeInMb < 1f) {
            return roundOneDecimal(safeSizeInMb * 1024f);
        }
        return roundOneDecimal(safeSizeInMb);
    }

    default FileSizeUnit toDisplaySizeUnit(Float sizeInMb) {
        float safeSizeInMb = sizeInMb == null ? 0f : sizeInMb;
        return safeSizeInMb < 1f ? FileSizeUnit.KB : FileSizeUnit.MB;
    }

    default FileType mapFileType(FileType fileType) {
        return fileType == null ? FileType.OTHER : fileType;
    }

    default float roundOneDecimal(float value) {
        return Math.round(value * 10f) / 10f;
    }

    default String buildFileLocation(Document document) {
        Folder folder = document.getFolder();
        if (folder == null) {
            return StorageConstants.ROOT_FOLDER_NAME;
        }
        return buildFolderPath(folder);
    }

    default String buildFolderPath(Folder folder) {
        if (folder.getParent() == null) {
            return folder.getFolderName();
        }
        return buildFolderPath(folder.getParent()) + "/" + folder.getFolderName();
    }
}
