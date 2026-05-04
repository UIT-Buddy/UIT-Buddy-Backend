package com.uit.buddy.service.note.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.buddy.dto.request.note.UpsertNoteRequest;
import com.uit.buddy.dto.response.note.NoteResponse;
import com.uit.buddy.entity.note.Note;
import com.uit.buddy.repository.note.NoteRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private static final String MSSV = "22100001";
    private UUID noteId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        noteId = UUID.randomUUID();
        now = LocalDateTime.now();
    }

    @Test
    void shouldReturnEmptyNoteWhenNoteNotFound() {
        when(noteRepository.findByMssv(MSSV)).thenReturn(Optional.empty());

        NoteResponse result = noteService.getNote(MSSV);

        assertThat(result.mssv()).isEqualTo(MSSV);
        assertThat(result.content()).isEmpty();
        assertThat(result.updatedAt()).isNull();
    }

    @Test
    void shouldReturnExistingNote() {
        Note existingNote = createNote(MSSV, "My existing note content", now);
        when(noteRepository.findByMssv(MSSV)).thenReturn(Optional.of(existingNote));

        NoteResponse result = noteService.getNote(MSSV);

        assertThat(result.mssv()).isEqualTo(MSSV);
        assertThat(result.content()).isEqualTo("My existing note content");
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldReturnEmptyStringWhenContentIsNull() {
        Note noteWithNullContent = createNote(MSSV, null, now);
        when(noteRepository.findByMssv(MSSV)).thenReturn(Optional.of(noteWithNullContent));

        NoteResponse result = noteService.getNote(MSSV);

        assertThat(result.mssv()).isEqualTo(MSSV);
        assertThat(result.content()).isEmpty();
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldCreateNewNoteWhenNoteDoesNotExist() {
        UpsertNoteRequest request = new UpsertNoteRequest("New note content");
        Note newNote = createNote(MSSV, "New note content", now);

        when(noteRepository.findByMssv(MSSV)).thenReturn(Optional.empty());
        when(noteRepository.saveAndFlush(any(Note.class))).thenReturn(newNote);

        NoteResponse result = noteService.upsertNote(MSSV, request);

        assertThat(result.mssv()).isEqualTo(MSSV);
        assertThat(result.content()).isEqualTo("New note content");
        assertThat(result.updatedAt()).isEqualTo(now);
        verify(noteRepository).saveAndFlush(any(Note.class));
    }

    @Test
    void shouldUpdateExistingNote() {
        Note existingNote = createNote(MSSV, "Old content", now.minusDays(1));
        UpsertNoteRequest request = new UpsertNoteRequest("Updated content");

        Note updatedNote = createNote(MSSV, "Updated content", now);

        when(noteRepository.findByMssv(MSSV)).thenReturn(Optional.of(existingNote));
        when(noteRepository.saveAndFlush(existingNote)).thenReturn(updatedNote);

        NoteResponse result = noteService.upsertNote(MSSV, request);

        assertThat(result.mssv()).isEqualTo(MSSV);
        assertThat(result.content()).isEqualTo("Updated content");
        assertThat(result.updatedAt()).isEqualTo(now);
        assertThat(existingNote.getContent()).isEqualTo("Updated content");
        verify(noteRepository).saveAndFlush(existingNote);
    }

    @Test
    void shouldHandleEmptyContentInUpsert() {
        UpsertNoteRequest request = new UpsertNoteRequest("");
        Note newNote = createNote(MSSV, "", now);

        when(noteRepository.findByMssv(MSSV)).thenReturn(Optional.empty());
        when(noteRepository.saveAndFlush(any(Note.class))).thenReturn(newNote);

        NoteResponse result = noteService.upsertNote(MSSV, request);

        assertThat(result.mssv()).isEqualTo(MSSV);
        assertThat(result.content()).isEmpty();
        assertThat(result.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldPreserveNullContentAsEmptyString() {
        Note noteWithNullContent = createNote(MSSV, null, now);
        UpsertNoteRequest request = new UpsertNoteRequest("New content");

        Note updatedNote = createNote(MSSV, "New content", now);

        when(noteRepository.findByMssv(MSSV)).thenReturn(Optional.of(noteWithNullContent));
        when(noteRepository.saveAndFlush(noteWithNullContent)).thenReturn(updatedNote);

        NoteResponse result = noteService.upsertNote(MSSV, request);

        assertThat(result.content()).isEqualTo("New content");
    }

    private Note createNote(String mssv, String content, LocalDateTime updatedAt) {
        Note note = Note.builder().mssv(mssv).content(content).build();
        note.setId(noteId);
        note.setCreatedAt(updatedAt.minusDays(1));
        note.setUpdatedAt(updatedAt);
        return note;
    }
}
