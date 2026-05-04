package com.uit.buddy.repository.note;

import com.uit.buddy.entity.note.Note;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

        Optional<Note> findByMssv(String mssv);
}
