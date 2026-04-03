package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByMssvAndFolderId(String mssv, UUID folderId);

    List<Document> findByFolderId(UUID folderId);

    Page<Document> findByFolderId(UUID folderId, Pageable pageable);

    Optional<Document> findByIdAndMssv(UUID id, String mssv);

    Page<Document> findByMssvAndFileNameContainingIgnoreCase(String mssv, String keyword, Pageable pageable);

    Page<Document> findByMssv(String mssv, Pageable pageable);
}
