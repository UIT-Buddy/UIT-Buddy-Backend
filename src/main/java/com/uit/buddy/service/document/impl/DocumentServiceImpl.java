package com.uit.buddy.service.document.impl;

import com.uit.buddy.client.CometChatClient;
import com.uit.buddy.constant.StorageConstants;
import com.uit.buddy.dto.request.client.CometChatSendMessageRequest;
import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.request.document.ShareResourceRequest;
import com.uit.buddy.dto.request.document.ShareResourceViaMessageRequest;
import com.uit.buddy.dto.request.document.UnshareResourceRequest;
import com.uit.buddy.dto.request.document.UpdateFileRequest;
import com.uit.buddy.dto.request.document.UpdateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.DocumentUploadResult;
import com.uit.buddy.dto.response.document.SharedFolderResponse;
import com.uit.buddy.dto.response.document.SharedUserResponse;
import com.uit.buddy.dto.response.document.UpdateFolderResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse.FileResponse;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse.PaginationMeta;
import com.uit.buddy.entity.document.Document;
import com.uit.buddy.entity.document.Folder;
import com.uit.buddy.entity.document.ShareDocument;
import com.uit.buddy.entity.document.ShareFolder;
import com.uit.buddy.enums.AccessRole;
import com.uit.buddy.enums.DocumentResourceType;
import com.uit.buddy.enums.ShareTargetType;
import com.uit.buddy.exception.document.DocumentErrorCode;
import com.uit.buddy.exception.document.DocumentException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.document.DocumentMapper;
import com.uit.buddy.mapper.user.StudentMapper;
import com.uit.buddy.repository.document.DocumentRepository;
import com.uit.buddy.repository.document.FolderRepository;
import com.uit.buddy.repository.document.ShareDocumentRepository;
import com.uit.buddy.repository.document.ShareFolderRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.document.DocumentService;
import com.uit.buddy.service.file.FileService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.uit.buddy.dto.request.document.UpdateFolderRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final ShareDocumentRepository shareDocumentRepository;
    private final ShareFolderRepository shareFolderRepository;
    private final StudentRepository studentRepository;
    private final CometChatClient cometChatClient;
    private final FileService fileService;
    private final DocumentMapper documentMapper;
    private final StudentMapper studentMapper;

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

        List<DocumentUploadResult> uploadResults = fileService.uploadMultipleDocuments(request.files());

        List<DocumentFileResponse> responses = new ArrayList<>();
        int fileIndex = 0;
        for (MultipartFile file : request.files()) {
            if (file == null || file.isEmpty()) {
                throw new UserException(UserErrorCode.FILE_EMPTY);
            }

            String fileName = StorageConstants.normalizeFileName(file.getOriginalFilename());
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
    public ViewFolderDetailResponse viewFolderDetail(String mssv, UUID folderId, Pageable pageable) {
        Folder folder = resolveAccessibleFolder(mssv, folderId);

        List<Folder> childFolders = folderRepository.findByParentId(folder.getId());
        List<Document> childFiles = documentRepository.findByFolderId(folder.getId());

        record FolderDetailItem(Folder folderItem, Document fileItem, String name) {
        }

        List<FolderDetailItem> allItems = new ArrayList<>(childFolders.size() + childFiles.size());
        childFolders.forEach(child -> allItems.add(new FolderDetailItem(child, null, child.getFolderName())));
        childFiles.forEach(file -> allItems.add(new FolderDetailItem(null, file, file.getFileName())));

        Sort.Order order = pageable.getSort().stream().findFirst().orElse(Sort.Order.desc("createdAt"));
        String property = order.getProperty();
        Comparator<FolderDetailItem> comparator;
        if ("name".equalsIgnoreCase(property) || "folderName".equalsIgnoreCase(property)
                || "fileName".equalsIgnoreCase(property)) {
            comparator = Comparator.comparing(item -> item.name() == null ? "" : item.name().toLowerCase(Locale.ROOT));
        } else {
            comparator = Comparator.comparing(item -> item.folderItem() != null ? item.folderItem().getCreatedAt()
                    : item.fileItem().getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()));
        }
        if (order.getDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }
        allItems.sort(comparator);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allItems.size());
        if (start > end) {
            start = end;
        }

        List<FolderDetailItem> pagedItems = allItems.subList(start, end);
        List<ViewFolderDetailResponse.FolderResponse> folderResponses = pagedItems.stream()
                .filter(item -> item.folderItem() != null)
                .map(item -> documentMapper.toFolderResponse(item.folderItem())).toList();
        List<FileResponse> fileResponses = pagedItems.stream().filter(item -> item.fileItem() != null)
                .map(item -> documentMapper.toFileResponse(item.fileItem())).toList();

        PageImpl<FolderDetailItem> pagedResult = new PageImpl<>(pagedItems, pageable, allItems.size());
        PaginationMeta paging = new PaginationMeta(pageable.getPageNumber() + 1, pageable.getPageSize(),
                pagedResult.getTotalElements(), pagedResult.getTotalPages());

        return new ViewFolderDetailResponse(folder.getId(), folder.getFolderName(), buildFolderPath(folder),
                folder.getParent() != null ? folder.getParent().getId() : null, folderResponses, fileResponses, paging);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDownloadUrl(String mssv, UUID fileId) {
        Document document = documentRepository.findById(fileId)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));

        if (!hasDocumentAccess(mssv, document)) {
            throw new DocumentException(DocumentErrorCode.FILE_ACCESS_DENIED);
        }

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

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentSearchResult> searchSharedWithMe(String mssv, String keyword, Pageable pageable) {
        List<ShareDocument> directShares = shareDocumentRepository.findByMssv(mssv);
        List<ShareFolder> folderShares = shareFolderRepository.findByMssv(mssv);

        Map<UUID, Document> documentsById = new LinkedHashMap<>();

        for (ShareDocument shareDocument : directShares) {
            Document document = shareDocument.getDocument();
            if (document != null) {
                documentsById.put(document.getId(), document);
            }
        }

        Set<UUID> folderIds = new HashSet<>();
        for (ShareFolder shareFolder : folderShares) {
            Folder folder = shareFolder.getFolder();
            if (folder == null) {
                continue;
            }
            collectDescendantFolderIds(folder.getId(), folderIds);
        }

        for (UUID folderId : folderIds) {
            for (Document document : documentRepository.findByFolderId(folderId)) {
                documentsById.put(document.getId(), document);
            }
        }

        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Document> filteredDocuments = new ArrayList<>(documentsById.values().stream()
                .filter(document -> matchesSharedSearch(document, normalizedKeyword)).toList());

        Sort.Order order = pageable.getSort().stream().findFirst().orElse(Sort.Order.desc("createdAt"));
        Comparator<Document> comparator = Comparator.comparing(Document::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder()));
        if (order.getDirection() == Sort.Direction.DESC) {
            comparator = comparator.reversed();
        }
        filteredDocuments.sort(comparator);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredDocuments.size());
        if (start > end) {
            start = end;
        }

        List<DocumentSearchResult> content = filteredDocuments.subList(start, end).stream()
                .map(documentMapper::toSearchResult).toList();
        return new PageImpl<>(content, pageable, filteredDocuments.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SharedFolderResponse> getSharedFoldersWithMe(String mssv, Pageable pageable) {
        return shareFolderRepository.findByMssv(mssv, pageable).map(shareFolder -> {
            Folder folder = shareFolder.getFolder();
            int itemCount = folder.getChildren().size() + folder.getFiles().size();

            return new SharedFolderResponse(folder.getId(), folder.getFolderName(), buildFolderPath(folder),
                    folder.getParent() != null ? folder.getParent().getId() : null, itemCount,
                    studentMapper.toStudentResponse(folder.getOwner()), shareFolder.getAccessRole(),
                    shareFolder.getCreatedAt());
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SharedUserResponse> getSharedUsers(String mssv, DocumentResourceType resourceType, UUID resourceId,
            Pageable pageable) {
        validateOwnership(mssv, resourceType, resourceId);
        List<SharedUserResponse> all = fetchSharedUsers(resourceType, resourceId);
        return new PageImpl<>(all, pageable, all.size());
    }

    @Override
    @Transactional
    public DocumentFileResponse updateDocument(String mssv, UUID documentId, UpdateFileRequest request) {
        Document document = documentRepository.findByIdAndMssv(documentId, mssv)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));
        String newFileName = request.fileName().trim();
        if (!newFileName.equalsIgnoreCase(document.getFileName())) {
            boolean nameExists = documentRepository.existsByFolderIdAndFileNameIgnoreCase(document.getFolderId(),
                    newFileName, documentId);
            if (nameExists) {
                throw new DocumentException(DocumentErrorCode.FILE_ALREADY_EXISTS);
            }
            document.setFileName(newFileName);
        }

        if (request.folderId() != null && !request.folderId().equals(document.getFolderId())) {
            Folder targetFolder = findOwnedFolder(mssv, request.folderId());
            boolean nameExistsInTarget = documentRepository.existsByFolderIdAndFileNameIgnoreCase(request.folderId(),
                    newFileName, documentId);
            if (nameExistsInTarget) {
                throw new DocumentException(DocumentErrorCode.FILE_ALREADY_EXISTS);
            }
            document.setFolder(targetFolder);
        }

        document = documentRepository.save(document);
        return documentMapper.toDocumentFileResponse(document);
    }

    @Override
    @Transactional
    public UpdateFolderResponse updateFolder(String mssv, UUID folderId, UpdateFolderRequest request) {
        Folder folder = findOwnedFolder(mssv, folderId);

        UUID currentParentId = folder.getParent() != null ? folder.getParent().getId() : null;
        if (request.parentFolderId() != null && !request.parentFolderId().equals(currentParentId)) {
            Folder targetFolder = findOwnedFolder(mssv, request.parentFolderId());
            folder.setParent(targetFolder);
        }

        folder.setFolderName(request.folderName());
        folder = folderRepository.save(folder);
        return documentMapper.toUpdateFolderResponse(folder);
    }

    @Override
    @Transactional
    public void deleteDocument(String mssv, UUID documentId) {
        Document document = documentRepository.findByIdAndMssv(documentId, mssv)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));

        // Delete S3 object
        fileService.deleteDocument(document.getFileUrl());

        // Delete DB record (cascades to share_document)
        documentRepository.delete(document);
    }

    @Override
    @Transactional
    public void shareResource(String mssv, ShareResourceRequest request) {
        validateShareTarget(mssv, request.targetMssv());

        if (request.resourceType() == DocumentResourceType.FILE) {
            shareDocument(mssv, request);
            return;
        }

        if (request.resourceType() == DocumentResourceType.FOLDER) {
            shareFolder(mssv, request);
            return;
        }

        throw new DocumentException(DocumentErrorCode.INVALID_SHARE_RESOURCE_TYPE);
    }

    @Override
    @Transactional
    public void shareResourceViaMessage(String mssv, ShareResourceViaMessageRequest request) {
        ShareTargetType receiverType = request.receiverType();
        String receiverId = request.receiverId();

        if (receiverType == ShareTargetType.USER) {
            validateShareTarget(mssv, receiverId);
            shareResource(mssv, new ShareResourceRequest(request.resourceType(), request.resourceId(), receiverId));
        }

        SharedResourceMetadata metadata = resolveOwnedResourceMetadata(mssv, request.resourceType(),
                request.resourceId());

        String messageText = request.content() != null && !request.content().isBlank() ? request.content()
                : defaultShareMessage(metadata);

        Map<String, Object> resourceMetadata = new LinkedHashMap<>();
        resourceMetadata.put("resourceType", metadata.resourceType().name());
        resourceMetadata.put("resourceId", metadata.resourceId().toString());
        resourceMetadata.put("resourceName", metadata.resourceName());
        resourceMetadata.put("resourcePath", metadata.resourcePath());
        if (metadata.url() != null && !metadata.url().isBlank()) {
            resourceMetadata.put("url", metadata.url());
        }

        CometChatSendMessageRequest messageRequest = CometChatSendMessageRequest.builder().receiver(receiverId)
                .receiverType(receiverType == ShareTargetType.USER ? "user" : "group").category("message").type("text")
                .data(Map.of("text", messageText, "metadata", resourceMetadata)).build();

        cometChatClient.sendMessage(messageRequest, mssv);
    }

    @Override
    @Transactional
    public void unshareResource(String mssv, UnshareResourceRequest request) {
        if (request.resourceType() == DocumentResourceType.FILE) {
            unshareDocument(mssv, request);
            return;
        }

        if (request.resourceType() == DocumentResourceType.FOLDER) {
            unshareFolder(mssv, request);
            return;
        }

        throw new DocumentException(DocumentErrorCode.INVALID_SHARE_RESOURCE_TYPE);
    }

    private void shareDocument(String mssv, ShareResourceRequest request) {
        Document document = documentRepository.findByIdAndMssv(request.resourceId(), mssv)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));

        Optional<ShareDocument> existingShare = shareDocumentRepository.findByDocumentIdAndMssv(request.resourceId(),
                request.targetMssv());

        ShareDocument shareDocument = existingShare.orElseGet(() -> ShareDocument.builder().document(document)
                .recipient(studentRepository.getReferenceById(request.targetMssv())).build());

        shareDocument.setAccessRole(AccessRole.VIEWER);
        shareDocumentRepository.save(shareDocument);
    }

    private void shareFolder(String mssv, ShareResourceRequest request) {
        Folder folder = findOwnedFolder(mssv, request.resourceId());

        Optional<ShareFolder> existingShare = shareFolderRepository.findByFolderIdAndMssv(request.resourceId(),
                request.targetMssv());

        ShareFolder shareFolder = existingShare.orElseGet(() -> ShareFolder.builder().folder(folder)
                .recipient(studentRepository.getReferenceById(request.targetMssv())).build());

        shareFolder.setAccessRole(AccessRole.VIEWER);
        shareFolderRepository.save(shareFolder);
    }

    private void unshareDocument(String mssv, UnshareResourceRequest request) {
        documentRepository.findByIdAndMssv(request.resourceId(), mssv)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));

        long deleted = shareDocumentRepository.deleteByDocumentIdAndMssv(request.resourceId(), request.targetMssv());
        if (deleted == 0) {
            throw new DocumentException(DocumentErrorCode.SHARE_NOT_FOUND);
        }
    }

    private void unshareFolder(String mssv, UnshareResourceRequest request) {
        findOwnedFolder(mssv, request.resourceId());

        long deleted = shareFolderRepository.deleteByFolderIdAndMssv(request.resourceId(), request.targetMssv());
        if (deleted == 0) {
            throw new DocumentException(DocumentErrorCode.SHARE_NOT_FOUND);
        }
    }

    private String buildFolderPath(Folder folder) {
        if (folder.getParent() == null) {
            return folder.getFolderName();
        }
        return (buildFolderPath(folder.getParent()) + "/" + folder.getFolderName());
    }

    private Folder findOwnedFolder(String mssv, UUID folderId) {
        return folderRepository.findByIdAndMssv(folderId, mssv)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FOLDER_NOT_FOUND));
    }

    private Folder resolveAccessibleFolder(String mssv, UUID folderId) {
        if (folderId == null) {
            return resolveTargetFolder(mssv, null);
        }

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FOLDER_NOT_FOUND));

        if (!isFolderAccessible(mssv, folder)) {
            throw new DocumentException(DocumentErrorCode.FOLDER_ACCESS_DENIED);
        }

        return folder;
    }

    private boolean hasDocumentAccess(String mssv, Document document) {
        if (mssv.equals(document.getMssv())) {
            return true;
        }

        if (shareDocumentRepository.existsByDocumentIdAndMssv(document.getId(), mssv)) {
            return true;
        }

        Folder folder = document.getFolder();
        while (folder != null) {
            if (shareFolderRepository.existsByFolderIdAndMssv(folder.getId(), mssv)) {
                return true;
            }
            folder = folder.getParent();
        }

        return false;
    }

    private boolean isFolderAccessible(String mssv, Folder folder) {
        if (mssv.equals(folder.getMssv())) {
            return true;
        }

        Folder current = folder;
        while (current != null) {
            if (shareFolderRepository.existsByFolderIdAndMssv(current.getId(), mssv)) {
                return true;
            }
            current = current.getParent();
        }

        return false;
    }

    private void collectDescendantFolderIds(UUID rootFolderId, Set<UUID> collectedFolderIds) {
        if (!collectedFolderIds.add(rootFolderId)) {
            return;
        }

        List<Folder> children = folderRepository.findByParentId(rootFolderId);
        for (Folder child : children) {
            collectDescendantFolderIds(child.getId(), collectedFolderIds);
        }
    }

    private boolean matchesSharedSearch(Document document, String normalizedKeyword) {
        if (normalizedKeyword.isBlank()) {
            return true;
        }

        String fileName = document.getFileName() == null ? "" : document.getFileName().toLowerCase(Locale.ROOT);
        return fileName.contains(normalizedKeyword);
    }

    private void validateShareTarget(String ownerMssv, String targetMssv) {
        if (ownerMssv.equalsIgnoreCase(targetMssv)) {
            throw new DocumentException(DocumentErrorCode.CANNOT_SHARE_WITH_SELF);
        }

        if (!studentRepository.existsById(targetMssv)) {
            throw new DocumentException(DocumentErrorCode.RECIPIENT_NOT_FOUND);
        }
    }

    private SharedResourceMetadata resolveOwnedResourceMetadata(String mssv, DocumentResourceType resourceType,
            UUID resourceId) {
        if (resourceType == DocumentResourceType.FILE) {
            Document document = documentRepository.findByIdAndMssv(resourceId, mssv)
                    .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));

            String resourcePath = document.getFolder() != null
                    ? buildFolderPath(document.getFolder()) + StorageConstants.PATH_SEPARATOR + document.getFileName()
                    : StorageConstants.ROOT_FOLDER_NAME + StorageConstants.PATH_SEPARATOR + document.getFileName();

            return new SharedResourceMetadata(DocumentResourceType.FILE, document.getId(), document.getFileName(),
                    resourcePath, document.getFileUrl());
        }

        if (resourceType == DocumentResourceType.FOLDER) {
            Folder folder = findOwnedFolder(mssv, resourceId);
            return new SharedResourceMetadata(DocumentResourceType.FOLDER, folder.getId(), folder.getFolderName(),
                    buildFolderPath(folder), null);
        }

        throw new DocumentException(DocumentErrorCode.INVALID_SHARE_RESOURCE_TYPE);
    }

    private String defaultShareMessage(SharedResourceMetadata metadata) {
        if (metadata.resourceType() == DocumentResourceType.FILE) {
            return "Shared a file: " + metadata.resourceName();
        }
        return "Shared a folder: " + metadata.resourceName();
    }

    private record SharedResourceMetadata(DocumentResourceType resourceType, UUID resourceId, String resourceName,
            String resourcePath, String url) {
    }

    private Folder resolveTargetFolder(String mssv, UUID folderId) {
        if (folderId != null) {
            return findOwnedFolder(mssv, folderId);
        }

        return folderRepository.findFirstByMssvAndParentIsNullAndFolderName(mssv, StorageConstants.ROOT_FOLDER_NAME)
                .orElseGet(() -> folderRepository.save(Folder.builder().owner(studentRepository.getReferenceById(mssv))
                        .folderName(StorageConstants.ROOT_FOLDER_NAME).parent(null).build()));
    }

    private void validateOwnership(String mssv, DocumentResourceType resourceType, UUID resourceId) {
        if (resourceType == DocumentResourceType.FILE) {
            documentRepository.findByIdAndMssv(resourceId, mssv)
                    .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));
            return;
        }
        if (resourceType == DocumentResourceType.FOLDER) {
            findOwnedFolder(mssv, resourceId);
            return;
        }
        throw new DocumentException(DocumentErrorCode.INVALID_SHARE_RESOURCE_TYPE);
    }

    private List<SharedUserResponse> fetchSharedUsers(DocumentResourceType resourceType, UUID resourceId) {
        if (resourceType == DocumentResourceType.FILE) {
            return shareDocumentRepository.findByDocumentId(resourceId).stream()
                    .map(sd -> new SharedUserResponse(studentMapper.toStudentResponse(sd.getRecipient()),
                            sd.getAccessRole(), sd.getCreatedAt()))
                    .toList();
        }
        if (resourceType == DocumentResourceType.FOLDER) {
            return shareFolderRepository.findByFolderId(resourceId).stream()
                    .map(sf -> new SharedUserResponse(studentMapper.toStudentResponse(sf.getRecipient()),
                            sf.getAccessRole(), sf.getCreatedAt()))
                    .toList();
        }
        throw new DocumentException(DocumentErrorCode.INVALID_SHARE_RESOURCE_TYPE);
    }
}
