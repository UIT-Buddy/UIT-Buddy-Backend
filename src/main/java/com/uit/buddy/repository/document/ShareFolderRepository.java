package com.uit.buddy.repository.document;

import com.uit.buddy.entity.document.ShareFolder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShareFolderRepository extends JpaRepository<ShareFolder, UUID> {
    List<ShareFolder> findByMssv(String mssv);

    Optional<ShareFolder> findByFolderIdAndMssv(UUID folderId, String mssv);

    boolean existsByFolderIdAndMssv(UUID folderId, String mssv);

    long deleteByFolderIdAndMssv(UUID folderId, String mssv);

    List<ShareFolder> findByFolderId(UUID folderId);
}
