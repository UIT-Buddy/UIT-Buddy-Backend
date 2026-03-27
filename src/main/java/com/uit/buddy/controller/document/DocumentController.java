package com.uit.buddy.controller.document;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.CreatedResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
import com.uit.buddy.service.document.DocumentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document", description = "Document management APIs")
public class DocumentController extends AbstractBaseController {
    private final DocumentService documentService;

    @PostMapping(value = "/folder")
    @Operation(summary = "Create a new folder", description = "Create a new folder")
    public ResponseEntity<CreatedResponse<Void>> createFolder(@AuthenticationPrincipal String mssv,
            @Valid @RequestBody CreateFolderRequest request) {
        documentService.createNewFolder(mssv, request);
        return created("Folder created successfully");
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new files", description = "Create new files and upload to cloudinary")
    public ResponseEntity<CreatedResponse<List<DocumentFileResponse>>> createFile(@AuthenticationPrincipal String mssv,
            @ModelAttribute CreateFileRequest request) {
        List<DocumentFileResponse> response = documentService.createNewFile(mssv, request);
        return created(response, "Files created successfully");
    }

    @GetMapping(value = "/folder")
    @Operation(summary = "View folder detail", description = "View detail of a folder including children folders and files")
    public ResponseEntity<SingleResponse<ViewFolderDetailResponse>> viewFolderDetail(
            @AuthenticationPrincipal String mssv,
            @RequestParam(required = false) UUID folderId) {
        ViewFolderDetailResponse response = documentService.viewFolderDetail(mssv, folderId);
        return successSingle(response, "Folder detail retrieved successfully");
    }

}
