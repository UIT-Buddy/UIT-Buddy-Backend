package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.ShareDocument;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareDocumentRepository extends JpaRepository<ShareDocument, UUID> {
}
