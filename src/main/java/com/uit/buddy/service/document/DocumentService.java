package com.uit.buddy.service.document;

import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.request.document.ShareResourceRequest;
import com.uit.buddy.dto.request.document.ShareResourceViaMessageRequest;
import com.uit.buddy.dto.request.document.UnshareResourceRequest;
import com.uit.buddy.dto.request.document.UpdateFileRequest;
import com.uit.buddy.dto.request.document.UpdateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.SharedFolderResponse;
import com.uit.buddy.dto.response.document.SharedUserResponse;
import com.uit.buddy.dto.response.document.UpdateFolderResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
import com.uit.buddy.enums.DocumentResourceType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DocumentService {
    UUID createNewFolder(String mssv, CreateFolderRequest request);

    List<DocumentFileResponse> createNewFile(String mssv, CreateFileRequest request);

    ViewFolderDetailResponse viewFolderDetail(String mssv, UUID folderId, Pageable pageable);

    String getDownloadUrl(String mssv, UUID fileId);

    Page<DocumentSearchResult> searchDocuments(String mssv, String keyword, Pageable pageable);

    Page<DocumentSearchResult> searchSharedWithMe(String mssv, String keyword, Pageable pageable);

    Page<SharedFolderResponse> getSharedFoldersWithMe(String mssv, Pageable pageable);

    Page<SharedUserResponse> getSharedUsers(String mssv, DocumentResourceType resourceType, UUID resourceId,
            Pageable pageable);

    void shareResource(String mssv, ShareResourceRequest request);

    void shareResourceViaMessage(String mssv, ShareResourceViaMessageRequest request);

    void unshareResource(String mssv, UnshareResourceRequest request);

    DocumentFileResponse updateDocument(String mssv, UUID documentId, UpdateFileRequest request);

    UpdateFolderResponse updateFolder(String mssv, UUID folderId, UpdateFolderRequest request);

    void deleteDocument(String mssv, UUID documentId);

}
