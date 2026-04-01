package com.uit.buddy.service.document.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.buddy.constant.StorageConstants;
import com.uit.buddy.dto.request.document.CreateFileRequest;
import com.uit.buddy.dto.request.document.CreateFolderRequest;
import com.uit.buddy.dto.response.document.DocumentFileResponse;
import com.uit.buddy.dto.response.document.DocumentSearchResult;
import com.uit.buddy.dto.response.document.DocumentUploadResult;
import com.uit.buddy.dto.response.document.ViewFolderDetailResponse;
import com.uit.buddy.entity.document.Document;
import com.uit.buddy.entity.document.Folder;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FileSizeUnit;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.document.DocumentErrorCode;
import com.uit.buddy.exception.document.DocumentException;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.mapper.document.DocumentMapper;
import com.uit.buddy.repository.document.DocumentRepository;
import com.uit.buddy.repository.document.FolderRepository;
import com.uit.buddy.repository.document.ShareDocumentRepository;
import com.uit.buddy.repository.document.ShareFolderRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.file.FileService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentServiceImplTest {

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ShareDocumentRepository shareDocumentRepository;

    @Mock
    private ShareFolderRepository shareFolderRepository;

    @Mock
    private FileService fileService;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private String mssv;
    private Student student;
    private Folder rootFolder;

    @BeforeEach
    void setUp() {
        mssv = "22100001";
        student = new Student();
        student.setMssv(mssv);

        rootFolder = new Folder();
        rootFolder.setId(UUID.randomUUID());
        rootFolder.setFolderName(StorageConstants.ROOT_FOLDER_NAME);
    }

    @Test
    void createNewFolder_zeroParentId_shouldCreateUnderStorageRootAndReturnId() {
        UUID createdFolderId = UUID.randomUUID();
        CreateFolderRequest request = new CreateFolderRequest("Docs", null);

        when(folderRepository.findFirstByMssvAndParentIsNullAndFolderName(mssv, StorageConstants.ROOT_FOLDER_NAME))
                .thenReturn(Optional.of(rootFolder));
        when(folderRepository.existsByMssvAndParentIdAndFolderNameIgnoreCase(mssv, rootFolder.getId(), "Docs"))
                .thenReturn(false);
        when(studentRepository.getReferenceById(mssv)).thenReturn(student);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> {
            Folder saved = invocation.getArgument(0);
            saved.setId(createdFolderId);
            return saved;
        });

        UUID result = documentService.createNewFolder(mssv, request);

        assertThat(result).isEqualTo(createdFolderId);
        verify(folderRepository).save(any(Folder.class));
    }

    @Test
    void createNewFolder_oneParentId_shouldCreateUnderProvidedParent() {
        UUID parentId = UUID.randomUUID();
        UUID createdFolderId = UUID.randomUUID();
        Folder parent = new Folder();
        parent.setId(parentId);
        CreateFolderRequest request = new CreateFolderRequest("Semester 2", parentId);

        when(folderRepository.findByIdAndMssv(parentId, mssv)).thenReturn(Optional.of(parent));
        when(folderRepository.existsByMssvAndParentIdAndFolderNameIgnoreCase(mssv, parentId, "Semester 2"))
                .thenReturn(false);
        when(studentRepository.getReferenceById(mssv)).thenReturn(student);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> {
            Folder saved = invocation.getArgument(0);
            saved.setId(createdFolderId);
            return saved;
        });

        UUID result = documentService.createNewFolder(mssv, request);

        assertThat(result).isEqualTo(createdFolderId);
        verify(folderRepository).findByIdAndMssv(parentId, mssv);
    }

    @Test
    void createNewFolder_duplicateName_shouldThrowDocumentException() {
        CreateFolderRequest request = new CreateFolderRequest("Docs", null);

        when(folderRepository.findFirstByMssvAndParentIsNullAndFolderName(mssv, StorageConstants.ROOT_FOLDER_NAME))
                .thenReturn(Optional.of(rootFolder));
        when(folderRepository.existsByMssvAndParentIdAndFolderNameIgnoreCase(mssv, rootFolder.getId(), "Docs"))
                .thenReturn(true);

        assertThatThrownBy(() -> documentService.createNewFolder(mssv, request)).isInstanceOf(DocumentException.class)
                .extracting("code").isEqualTo(DocumentErrorCode.FOLDER_ALREADY_EXISTS.getCode());

        verify(folderRepository, never()).save(any(Folder.class));
    }

    @Test
    void createNewFile_zeroFiles_shouldThrowUserException() {
        CreateFileRequest request = new CreateFileRequest(new ArrayList<>(), UUID.randomUUID());

        assertThatThrownBy(() -> documentService.createNewFile(mssv, request)).isInstanceOf(UserException.class)
                .extracting("code").isEqualTo(UserErrorCode.FILE_EMPTY.getCode());
    }

    @Test
    void createNewFile_oneFile_shouldCreateOneDocument() {
        UUID folderId = UUID.randomUUID();
        Folder folder = new Folder();
        folder.setId(folderId);

        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("one.pdf");

        CreateFileRequest request = new CreateFileRequest(List.of(file), folderId);
        List<DocumentUploadResult> uploads = List.of(new DocumentUploadResult("https://one", 1.2f, FileType.WORD));
        DocumentFileResponse mapped = new DocumentFileResponse(UUID.randomUUID(), "one.pdf", "https://one", folderId,
                1.2f, FileSizeUnit.MB, FileType.WORD);

        when(folderRepository.findByIdAndMssv(folderId, mssv)).thenReturn(Optional.of(folder));
        when(fileService.uploadMultipleDocuments(request.files())).thenReturn(uploads);
        when(studentRepository.getReferenceById(mssv)).thenReturn(student);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentMapper.toDocumentFileResponse(any(Document.class))).thenReturn(mapped);

        List<DocumentFileResponse> result = documentService.createNewFile(mssv, request);

        assertThat(result).hasSize(1);
        verify(documentRepository, times(1)).save(any(Document.class));
        verify(documentMapper, times(1)).toDocumentFileResponse(any(Document.class));
    }

    @Test
    void createNewFile_manyFiles_shouldCreateAllDocuments() {
        UUID folderId = UUID.randomUUID();
        Folder folder = new Folder();
        folder.setId(folderId);

        MultipartFile file1 = org.mockito.Mockito.mock(MultipartFile.class);
        MultipartFile file2 = org.mockito.Mockito.mock(MultipartFile.class);
        when(file1.isEmpty()).thenReturn(false);
        when(file2.isEmpty()).thenReturn(false);
        when(file1.getOriginalFilename()).thenReturn("a.pdf");
        when(file2.getOriginalFilename()).thenReturn("b.pdf");

        CreateFileRequest request = new CreateFileRequest(List.of(file1, file2), folderId);
        List<DocumentUploadResult> uploads = List.of(new DocumentUploadResult("https://a", 0.5f, FileType.WORD),
                new DocumentUploadResult("https://b", 2.0f, FileType.PPT));

        when(folderRepository.findByIdAndMssv(folderId, mssv)).thenReturn(Optional.of(folder));
        when(fileService.uploadMultipleDocuments(request.files())).thenReturn(uploads);
        when(studentRepository.getReferenceById(mssv)).thenReturn(student);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documentMapper.toDocumentFileResponse(any(Document.class))).thenAnswer(invocation -> {
            Document d = invocation.getArgument(0);
            return new DocumentFileResponse(UUID.randomUUID(), d.getFileName(), d.getFileUrl(), d.getFolderId(),
                    d.getFileSize(), FileSizeUnit.MB, d.getFileType());
        });

        List<DocumentFileResponse> result = documentService.createNewFile(mssv, request);

        assertThat(result).hasSize(2);
        verify(documentRepository, times(2)).save(any(Document.class));
    }

    @Test
    void viewFolderDetail_zeroChildrenAndZeroFiles_shouldReturnEmptyLists() {
        UUID folderId = UUID.randomUUID();
        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setFolderName("Storage");
        folder.setMssv(mssv);

        Pageable pageable = PageRequest.of(0, 15);

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
        when(folderRepository.findByParentId(folderId)).thenReturn(List.of());
        when(documentRepository.findByFolderId(folderId)).thenReturn(List.of());

        ViewFolderDetailResponse result = documentService.viewFolderDetail(mssv, folderId, pageable);

        assertThat(result.folderId()).isEqualTo(folderId);
        assertThat(result.folders()).isEmpty();
        assertThat(result.files()).isEmpty();
    }

    @Test
    void viewFolderDetail_manyChildrenAndFiles_shouldReturnMappedData() {
        UUID folderId = UUID.randomUUID();
        Folder root = new Folder();
        root.setFolderName("Storage");
        Folder folder = new Folder();
        folder.setId(folderId);
        folder.setFolderName("Semester");
        folder.setParent(root);
        folder.setMssv(mssv);

        Folder child1 = new Folder();
        child1.setId(UUID.randomUUID());
        child1.setFolderName("Week1");
        Folder child2 = new Folder();
        child2.setId(UUID.randomUUID());
        child2.setFolderName("Week2");

        Document doc1 = new Document();
        doc1.setId(UUID.randomUUID());
        doc1.setFileName("a.pdf");
        Document doc2 = new Document();
        doc2.setId(UUID.randomUUID());
        doc2.setFileName("b.ppt");

        Pageable pageable = PageRequest.of(0, 15);

        when(folderRepository.findById(folderId)).thenReturn(Optional.of(folder));
        when(folderRepository.findByParentId(folderId)).thenReturn(List.of(child1, child2));
        when(documentRepository.findByFolderId(folderId)).thenReturn(List.of(doc1, doc2));
        when(documentMapper.toFolderResponse(any(Folder.class))).thenAnswer(invocation -> {
            Folder f = invocation.getArgument(0);
            return new ViewFolderDetailResponse.FolderResponse(f.getId(), f.getFolderName(), 0);
        });
        when(documentMapper.toFileResponse(any(Document.class))).thenAnswer(invocation -> {
            Document d = invocation.getArgument(0);
            return new ViewFolderDetailResponse.FileResponse(d.getId(), d.getFileName(), "url", 1.0f, FileSizeUnit.MB,
                    FileType.OTHER);
        });

        ViewFolderDetailResponse result = documentService.viewFolderDetail(mssv, folderId, pageable);

        assertThat(result.folderPath()).isEqualTo("Storage/Semester");
        assertThat(result.folders()).hasSize(2);
        assertThat(result.files()).hasSize(2);
    }

    @Test
    void getDownloadUrl_oneDocument_shouldReturnFileUrl() {
        UUID fileId = UUID.randomUUID();
        Document document = new Document();
        document.setId(fileId);
        document.setFileUrl("https://download");
        document.setMssv(mssv);

        when(documentRepository.findById(fileId)).thenReturn(Optional.of(document));

        String result = documentService.getDownloadUrl(mssv, fileId);

        assertThat(result).isEqualTo("https://download");
    }

    @Test
    void getDownloadUrl_zeroDocument_shouldThrowDocumentException() {
        UUID fileId = UUID.randomUUID();
        when(documentRepository.findById(fileId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getDownloadUrl(mssv, fileId)).isInstanceOf(DocumentException.class)
                .extracting("code").isEqualTo(DocumentErrorCode.FILE_NOT_FOUND.getCode());
    }

    @Test
    void searchDocuments_zeroKeyword_shouldUseFindByMssv() {
        Pageable pageable = PageRequest.of(0, 10);
        Document document = new Document();
        document.setId(UUID.randomUUID());
        document.setFileName("storage-note.pdf");

        Page<Document> page = new PageImpl<>(List.of(document));
        DocumentSearchResult mapped = new DocumentSearchResult("storage-note.pdf", "Storage", null, document.getId(),
                "u");

        when(documentRepository.findByMssv(mssv, pageable)).thenReturn(page);
        when(documentMapper.toSearchResult(any(Document.class))).thenReturn(mapped);

        Page<DocumentSearchResult> result = documentService.searchDocuments(mssv, "   ", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(documentRepository).findByMssv(mssv, pageable);
        verify(documentRepository, never()).findByMssvAndFileNameContainingIgnoreCase(anyString(), anyString(),
                any(Pageable.class));
    }

    @Test
    void searchDocuments_oneKeyword_shouldUseTrimmedKeywordSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Document document = new Document();
        document.setId(UUID.randomUUID());
        document.setFileName("algorithm.pdf");

        Page<Document> page = new PageImpl<>(List.of(document));
        when(documentRepository.findByMssvAndFileNameContainingIgnoreCase(mssv, "algorithm", pageable))
                .thenReturn(page);
        when(documentMapper.toSearchResult(any(Document.class)))
                .thenReturn(new DocumentSearchResult("algorithm.pdf", "Storage", null, document.getId(), "u"));

        Page<DocumentSearchResult> result = documentService.searchDocuments(mssv, "  algorithm  ", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(documentRepository).findByMssvAndFileNameContainingIgnoreCase(eq(mssv), eq("algorithm"), eq(pageable));
    }
}
