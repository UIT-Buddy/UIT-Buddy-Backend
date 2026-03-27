package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.ShareDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShareDocumentRepository extends JpaRepository<ShareDocument, UUID> {
}
