package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.Document;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByMssvAndFolderId(String mssv, UUID folderId);

    Optional<Document> findByIdAndMssv(UUID id, String mssv);

    @Query(value = """
            SELECT * FROM documents d
            WHERE d.mssv = :mssv
            AND LOWER(d.file_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """, nativeQuery = true)
    Page<Document> findByMssvAndFileNameContainingIgnoreCase(String mssv, String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM documents d WHERE d.mssv = :mssv", nativeQuery = true)
    Page<Document> findByMssv(String mssv, Pageable pageable);
}
