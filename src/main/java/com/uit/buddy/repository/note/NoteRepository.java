package com.uit.buddy.repository.note;

import com.uit.buddy.entity.note.Note;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    Optional<Note> findByIdAndMssv(UUID id, String mssv);

    List<Note> findByMssvOrderByUpdatedAtDesc(String mssv);

    @Query("""
            SELECT n FROM Note n
            WHERE n.mssv = :mssv
              AND (:nodeId IS NULL OR n.nodeId = :nodeId)
              AND (:keyword IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(COALESCE(n.content, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Note> searchNotes(@Param("mssv") String mssv, @Param("nodeId") UUID nodeId, @Param("keyword") String keyword,
            Pageable pageable);
}
