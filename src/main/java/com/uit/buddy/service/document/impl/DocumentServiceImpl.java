package com.uit.buddy.service.document.impl;

import com.uit.buddy.constant.CloudinaryConstants;
import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.DocumentUploadResult;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse.FileResponse;
import com.uit.buddy.entity.document.Document;
import com.uit.buddy.entity.document.Folder;
import com.uit.buddy.exception.document.DocumentErrorCode;
import com.uit.buddy.exception.document.DocumentException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.document.DocumentMapper;
import com.uit.buddy.repository.document.DocumentRepository;
import com.uit.buddy.repository.document.FolderRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import com.uit.buddy.service.document.DocumentService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final StudentRepository studentRepository;
    private final CloudinaryService cloudinaryService;
    private final DocumentMapper documentMapper;

    @Override
    @Transactional
    public UUID createNewFolder(String mssv, CreateFolderRequest request) {
        Folder parent = request.parentFolderId() != null ? findOwnedFolder(mssv, request.parentFolderId())
                : resolveTargetFolder(mssv, null);
        String folderName = request.folderName().trim();

        boolean folderExists = folderRepository.existsByMssvAndParentIdAndFolderNameIgnoreCase(mssv, parent.getId(),
                folderName);
        if (folderExists) {
            throw new DocumentException(DocumentErrorCode.FOLDER_ALREADY_EXISTS);
        }

        Folder folder = Folder.builder().owner(studentRepository.getReferenceById(mssv)).folderName(folderName)
                .parent(parent).build();

        folderRepository.save(folder);
        log.info("[Document Service] Created folder {} for mssv {}", folder.getFolderName(), mssv);
        return folder.getId();
    }

    @Override
    @Transactional
    public List<DocumentFileResponse> createNewFile(String mssv, CreateFileRequest request) {
        if (request.files() == null || request.files().isEmpty()) {
            throw new UserException(UserErrorCode.FILE_EMPTY);
        }

        Folder folder = resolveTargetFolder(mssv, request.folderId());

        List<DocumentUploadResult> uploadResults = cloudinaryService.uploadMultipleDocuments(request.files());

        List<DocumentFileResponse> responses = new ArrayList<>();
        int fileIndex = 0;
        for (MultipartFile file : request.files()) {
            if (file == null || file.isEmpty()) {
                throw new UserException(UserErrorCode.FILE_EMPTY);
            }

            String fileName = CloudinaryConstants.normalizeFileName(file.getOriginalFilename());
            DocumentUploadResult uploadResult = uploadResults.get(fileIndex);

            Document document = Document.builder().owner(studentRepository.getReferenceById(mssv)).folder(folder)
                    .fileName(fileName).fileUrl(uploadResult.fileUrl()).fileSize(uploadResult.fileSize())
                    .fileType(uploadResult.fileType()).build();

            document = documentRepository.save(document);

            responses.add(documentMapper.toDocumentFileResponse(document));
            fileIndex++;
        }
        return responses;
    }

    @Override
    @Transactional
    public ViewFolderDetailResponse viewFolderDetail(String mssv, UUID folderId) {
        Folder folder = resolveTargetFolder(mssv, folderId);

        List<Folder> childFolders = folderRepository.findByMssvAndParentId(mssv, folder.getId());
        List<Document> files = documentRepository.findByMssvAndFolderId(mssv, folder.getId());

        List<ViewFolderDetailResponse.FolderResponse> folderResponses = childFolders.stream()
                .map(documentMapper::toFolderResponse).toList();

        List<FileResponse> fileResponses = files.stream().map(documentMapper::toFileResponse).toList();

        return new ViewFolderDetailResponse(folder.getId(), folder.getFolderName(), buildFolderPath(folder),
                folder.getParent() != null ? folder.getParent().getId() : null, folderResponses, fileResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDownloadUrl(String mssv, UUID fileId) {
        Document document = documentRepository.findByIdAndMssv(fileId, mssv)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));
        return document.getFileUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentSearchResult> searchDocuments(String mssv, String keyword, Pageable pageable) {
        log.info("[Document Service] Searching documents for mssv {} with keyword '{}'", mssv, keyword);
        if (keyword == null || keyword.isBlank()) {
            return documentRepository.findByMssv(mssv, pageable).map(documentMapper::toSearchResult);
        }

        return documentRepository.findByMssvAndFileNameContainingIgnoreCase(mssv, keyword.trim(), pageable)
                .map(documentMapper::toSearchResult);
    }

    private String buildFolderPath(Folder folder) {
        if (folder.getParent() == null) {
            return folder.getFolderName();
        }
        return buildFolderPath(folder.getParent()) + "/" + folder.getFolderName();
    }

    private Folder findOwnedFolder(String mssv, UUID folderId) {
        return folderRepository.findByIdAndMssv(folderId, mssv)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FOLDER_NOT_FOUND));
    }

    private Folder resolveTargetFolder(String mssv, UUID folderId) {
        if (folderId != null) {
            return findOwnedFolder(mssv, folderId);
        }

        return folderRepository.findFirstByMssvAndParentIsNullAndFolderName(mssv, CloudinaryConstants.ROOT_FOLDER_NAME)
                .orElseGet(() -> folderRepository.save(Folder.builder().owner(studentRepository.getReferenceById(mssv))
                        .folderName(CloudinaryConstants.ROOT_FOLDER_NAME).parent(null).build()));
    }

}
