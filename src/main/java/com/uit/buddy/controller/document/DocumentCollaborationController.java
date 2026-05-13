package com.uit.buddy.controller.document;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.document.UpdateDocumentContentRequest;
import com.uit.buddy.dto.response.document.DocumentContentResponse;
import com.uit.buddy.service.document.DocumentCollaborationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Collaboration", description = "APIs for collaborative document editing")
public class DocumentCollaborationController extends AbstractBaseController {

    private final DocumentCollaborationService documentCollaborationService;

    @GetMapping("/{documentId}/content")
    @Operation(summary = "Get document content for editing")
    public ResponseEntity<SingleResponse<DocumentContentResponse>> getDocumentContent(@PathVariable UUID documentId,
            @AuthenticationPrincipal String mssv) {

        log.info("[GET /api/documents/{}/content] Getting document content for mssv: {}", documentId, mssv);

        DocumentContentResponse response = documentCollaborationService.getDocumentContent(documentId, mssv);

        return successSingle(response, "Document content retrieved successfully");
    }

    @PatchMapping("/{documentId}/content")
    @Operation(summary = "Update document content (auto-save)")
    public ResponseEntity<SingleResponse<DocumentContentResponse>> updateDocumentContent(@PathVariable UUID documentId,
            @Valid @RequestBody UpdateDocumentContentRequest request, @AuthenticationPrincipal String mssv) {

        log.info("[PATCH /api/documents/{}/content] Updating document content for mssv: {}, version: {}", documentId,
                mssv, request.getVersion());

        DocumentContentResponse response = documentCollaborationService.updateDocumentContent(documentId, request,
                mssv);

        return successSingle(response, "Document content updated successfully");
    }
}
