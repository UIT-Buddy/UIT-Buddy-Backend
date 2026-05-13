package com.uit.buddy.service.document;

import com.uit.buddy.dto.request.document.UpdateDocumentContentRequest;
import com.uit.buddy.dto.response.document.DocumentContentResponse;

import java.util.UUID;

public interface DocumentCollaborationService {

    DocumentContentResponse getDocumentContent(UUID documentId, String mssv);

    DocumentContentResponse updateDocumentContent(UUID documentId, UpdateDocumentContentRequest request, String mssv);

    void joinDocument(UUID documentId, String mssv);

    void leaveDocument(UUID documentId, String mssv);
}
