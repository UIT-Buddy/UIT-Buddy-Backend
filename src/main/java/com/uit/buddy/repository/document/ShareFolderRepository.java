package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.ShareFolder;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareFolderRepository extends JpaRepository<ShareFolder, UUID> {
}
