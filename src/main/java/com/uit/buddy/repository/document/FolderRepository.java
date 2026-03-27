package com.uit.buddy.repository.document;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.uit.buddy.entity.document.Folder;

public interface FolderRepository extends JpaRepository<Folder, UUID> {
    List<Folder> findByMssvAndParentId(String mssv, UUID parentId);

    List<Folder> findByMssvAndParentIsNull(String mssv);

    Optional<Folder> findFirstByMssvAndParentIsNullAndFolderName(String mssv, String folderName);

    Optional<Folder> findByIdAndMssv(UUID id, String mssv);

    boolean existsByMssvAndParentIdAndFolderNameIgnoreCase(String mssv, UUID parentId, String folderName);
}
