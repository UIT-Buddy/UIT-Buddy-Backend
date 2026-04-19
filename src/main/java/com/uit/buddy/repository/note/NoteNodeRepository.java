package com.uit.buddy.repository.note;

import com.uit.buddy.entity.note.NoteNode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteNodeRepository extends JpaRepository<NoteNode, UUID> {
    List<NoteNode> findByMssvOrderByCreatedAtAsc(String mssv);

    Optional<NoteNode> findByIdAndMssv(UUID id, String mssv);
}
