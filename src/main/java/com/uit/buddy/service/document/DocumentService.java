package com.uit.buddy.service.document;

import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {
    UUID createNewFolder(String mssv, CreateFolderRequest request);

    List<DocumentFileResponse> createNewFile(String mssv, CreateFileRequest request);

    ViewFolderDetailResponse viewFolderDetail(String mssv, UUID folderId);

    String getDownloadUrl(String mssv, UUID fileId);

    Page<DocumentSearchResult> searchDocuments(String mssv, String keyword, Pageable pageable);

}
