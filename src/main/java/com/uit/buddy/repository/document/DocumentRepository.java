package com.uit.buddy.repository.document;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uit.buddy.entity.document.Document;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByMssvAndFolderId(String mssv, UUID folderId);
}
