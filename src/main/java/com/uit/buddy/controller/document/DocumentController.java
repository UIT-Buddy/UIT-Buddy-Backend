package com.uit.buddy.controller.document;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CreatedResponse;
import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
        UUID folderId = documentService.createNewFolder(mssv, request);
        return created(folderId, "Folder created successfully");
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new files", description = "Create new files and upload to storage")
    public ResponseEntity<CreatedResponse<List<DocumentFileResponse>>> createFile(@AuthenticationPrincipal String mssv,
            @ModelAttribute CreateFileRequest request) {
        List<DocumentFileResponse> response = documentService.createNewFile(mssv, request);
        return created(response, "Files created successfully");
    }

    @GetMapping(value = "/folder")
    @Operation(summary = "View folder detail", description = "View detail of a folder including children folders and files")
    public ResponseEntity<SingleResponse<ViewFolderDetailResponse>> viewFolderDetail(
            @AuthenticationPrincipal String mssv, @RequestParam(required = false) UUID folderId) {
        ViewFolderDetailResponse response = documentService.viewFolderDetail(mssv, folderId);
        return successSingle(response, "Folder detail retrieved successfully");
    }

    @GetMapping(value = "/download/{fileId}")
    @Operation(summary = "Get download URL", description = "Get file download URL by file ID")
    public ResponseEntity<SingleResponse<String>> getDownloadUrl(@AuthenticationPrincipal String mssv,
            @PathVariable UUID fileId) {
        String url = documentService.getDownloadUrl(mssv, fileId);
        return successSingle(url, "Download URL retrieved successfully");
    }

    @GetMapping(value = "/search")
    @Operation(summary = "Search documents", description = "Search documents by file name")
    public ResponseEntity<PageResponse<DocumentSearchResult>> searchDocuments(@AuthenticationPrincipal String mssv,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "created_at") String sortBy, @RequestParam(required = false) String keyword) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        log.info("[Document Controller] Search documents for mssv {} with keyword '{}'", mssv, keyword);
        Page<DocumentSearchResult> response = documentService.searchDocuments(mssv, keyword, pageable);
        return paging(response, "Search documents successfully");
    }

}
