package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.ShareFolder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShareFolderRepository extends JpaRepository<ShareFolder, UUID> {
}
