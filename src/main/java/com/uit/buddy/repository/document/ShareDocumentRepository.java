package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.ShareDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareDocumentRepository extends JpaRepository<ShareDocument, UUID> {
    List<ShareDocument> findByMssv(String mssv);

    Optional<ShareDocument> findByDocumentIdAndMssv(UUID documentId, String mssv);

    boolean existsByDocumentIdAndMssv(UUID documentId, String mssv);

    long deleteByDocumentIdAndMssv(UUID documentId, String mssv);

    List<ShareDocument> findByDocumentId(UUID documentId);
}
