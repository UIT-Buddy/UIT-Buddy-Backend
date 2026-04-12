package com.uit.buddy.controller.document;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CreatedResponse;
import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.request.document.ShareResourceRequest;
import com.uit.buddy.dto.request.document.UnshareResourceRequest;
import com.uit.buddy.dto.request.document.UpdateFileRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.SharedUserResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
import com.uit.buddy.enums.DocumentResourceType;
import com.uit.buddy.service.document.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document", description = "Document management APIs")
public class DocumentController extends AbstractBaseController {
    private final DocumentService documentService;

    @PostMapping(value = "/folder")
    @Operation(summary = "Create a new folder", description = "Create a new folder")
    public ResponseEntity<CreatedResponse<UUID>> createFolder(@AuthenticationPrincipal String mssv,
            @Valid @RequestBody CreateFolderRequest request) {
        log.info("[POST /api/document/folder] Creating folder for mssv: {}", mssv);
        UUID folderId = documentService.createNewFolder(mssv, request);
        return created(folderId, "Folder created successfully");
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new files", description = "Create new files and upload to storage")
    public ResponseEntity<CreatedResponse<List<DocumentFileResponse>>> createFile(@AuthenticationPrincipal String mssv,
            @ModelAttribute CreateFileRequest request) {
        log.info("[POST /api/document/file] Creating files for mssv: {}", mssv);
        List<DocumentFileResponse> response = documentService.createNewFile(mssv, request);
        return created(response, "Files created successfully");
    }

    @GetMapping(value = "/folder")
    @Operation(summary = "View folder detail", description = "View detail of a folder including children folders and files")
    public ResponseEntity<SingleResponse<ViewFolderDetailResponse>> viewFolderDetail(
            @AuthenticationPrincipal String mssv, @RequestParam(required = false) UUID folderId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy) {
        log.info("[GET /api/document/folder] Viewing folder detail for mssv: {} and folderId: {}", mssv, folderId);
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        ViewFolderDetailResponse response = documentService.viewFolderDetail(mssv, folderId, pageable);
        return successSingle(response, "Folder detail retrieved successfully");
    }

    @PutMapping(value = "/{documentId}")
    @Operation(summary = "Update file", description = "Rename a file or move it to another folder")
    public ResponseEntity<SingleResponse<DocumentFileResponse>> updateDocument(@AuthenticationPrincipal String mssv,
            @PathVariable UUID documentId, @Valid @RequestBody UpdateFileRequest request) {
        log.info("[PUT /api/document/{}] Updating file for mssv: {}", documentId, mssv);
        DocumentFileResponse response = documentService.updateDocument(mssv, documentId, request);
        return successSingle(response, "File updated successfully");
    }

    @DeleteMapping(value = "/{documentId}")
    @Operation(summary = "Delete file", description = "Delete a file and its storage object")
    public ResponseEntity<SuccessResponse> deleteDocument(@AuthenticationPrincipal String mssv,
            @PathVariable UUID documentId) {
        log.info("[DELETE /api/document/{}] Deleting file for mssv: {}", documentId, mssv);
        documentService.deleteDocument(mssv, documentId);
        return success("File deleted successfully");
    }

    @GetMapping(value = "/download/{fileId}")
    @Operation(summary = "Get download URL", description = "Get file download URL by file ID")
    public ResponseEntity<SingleResponse<String>> getDownloadUrl(@AuthenticationPrincipal String mssv,
            @PathVariable UUID fileId) {
        log.info("[GET /api/document/download/{fileId}] Getting download URL for mssv: {} and fileId: {}", mssv,
                fileId);
        String url = documentService.getDownloadUrl(mssv, fileId);
        return successSingle(url, "Download URL retrieved successfully");
    }

    @GetMapping(value = "/search")
    @Operation(summary = "Search documents", description = "Search documents by file name")
    public ResponseEntity<PageResponse<DocumentSearchResult>> searchDocuments(@AuthenticationPrincipal String mssv,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(required = false) String keyword) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        log.info("[GET /api/document/search] Search documents for mssv {} with keyword '{}'", mssv, keyword);
        Page<DocumentSearchResult> response = documentService.searchDocuments(mssv, keyword, pageable);
        return paging(response, "Search documents successfully");
    }

    @GetMapping(value = "/shared-with-me")
    @Operation(summary = "Search documents shared with me", description = "Search by document name across resources shared with current user")
    public ResponseEntity<PageResponse<DocumentSearchResult>> searchSharedWithMe(@AuthenticationPrincipal String mssv,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "createdAt") String sortBy, @RequestParam(required = false) String keyword) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        log.info("[GET /api/document/shared-with-me] Search shared documents for mssv {} with keyword '{}'", mssv,
                keyword);
        Page<DocumentSearchResult> response = documentService.searchSharedWithMe(mssv, keyword, pageable);
        return paging(response, "Search shared documents successfully");
    }

    @PostMapping(value = "/share")
    @Operation(summary = "Share a resource", description = "Share a file or folder with another student")
    public ResponseEntity<SuccessResponse> shareResource(@AuthenticationPrincipal String mssv,
            @Valid @RequestBody ShareResourceRequest request) {
        log.info("[POST /api/document/share] Sharing {} {} by mssv {}", request.resourceType(), request.resourceId(),
                mssv);
        documentService.shareResource(mssv, request);
        return success("Resource shared successfully");
    }

    @DeleteMapping(value = "/share")
    @Operation(summary = "Unshare a resource", description = "Revoke shared access to a file or folder for a student")
    public ResponseEntity<SuccessResponse> unshareResource(@AuthenticationPrincipal String mssv,
            @Valid @RequestBody UnshareResourceRequest request) {
        log.info("[DELETE /api/document/share] Unsharing {} {} by mssv {}", request.resourceType(),
                request.resourceId(), mssv);
        documentService.unshareResource(mssv, request);
        return success("Resource unshared successfully");
    }

    @GetMapping(value = "/shared-user/{resourceType}/{resourceId}")
    @Operation(summary = "Get shared users", description = "View list of users a file or folder is shared with")
    public ResponseEntity<PageResponse<SharedUserResponse>> getSharedUsers(@AuthenticationPrincipal String mssv,
            @PathVariable DocumentResourceType resourceType, @PathVariable UUID resourceId,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "sharedAt") String sortBy) {
        log.info("[GET /api/document/shared-user/{}/{}] Getting shared users by mssv {}", resourceType, resourceId,
                mssv);
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<SharedUserResponse> response = documentService.getSharedUsers(mssv, resourceType, resourceId, pageable);
        return paging(response, "Shared users retrieved successfully");
    }

}
