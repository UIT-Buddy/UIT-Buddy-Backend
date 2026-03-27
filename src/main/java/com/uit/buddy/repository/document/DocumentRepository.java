package com.uit.buddy.repository.document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.uit.buddy.entity.document.Document;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByMssvAndFolderId(String mssv, UUID folderId);

    Optional<Document> findByIdAndMssv(UUID id, String mssv);

    @Query(value = """
            SELECT d.*, 
                   ts_rank(
                       to_tsvector('simple', coalesce(d.file_name,'')),
                       websearch_to_tsquery('simple', :keyword)
                   ) AS rank
            FROM documents d
            JOIN students s ON d.mssv = s.mssv
            WHERE to_tsvector('simple', coalesce(d.file_name,'')) 
                  @@ websearch_to_tsquery('simple', :keyword)
            ORDER BY rank DESC
            """,
            countQuery = """
            SELECT count(*) 
            FROM documents d
            WHERE to_tsvector('simple', coalesce(d.file_name,'')) 
                  @@ websearch_to_tsquery('simple', :keyword)
            """, nativeQuery = true)
    Page<Document> findByMssvAndFileNameContainingIgnoreCase(String mssv, String keyword, Pageable pageable);

    @Query(value = "SELECT * FROM documents d WHERE d.mssv = :mssv", nativeQuery = true)
    Page<Document> findByMssv(String mssv, Pageable pageable);
}
