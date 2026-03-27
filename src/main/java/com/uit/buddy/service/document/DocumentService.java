package com.uit.buddy.service.document;

import java.util.List;
import java.util.UUID;

import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;

public interface DocumentService {
    void createNewFolder(String mssv, CreateFolderRequest request);

    List<DocumentFileResponse> createNewFile(String mssv, CreateFileRequest request);

    ViewFolderDetailResponse viewFolderDetail(String mssv, UUID folderId);
}
