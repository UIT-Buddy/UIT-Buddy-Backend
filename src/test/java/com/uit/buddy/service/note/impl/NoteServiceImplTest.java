package com.uit.buddy.service.note.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.buddy.dto.request.note.CreateNoteRequest;
import com.uit.buddy.dto.request.note.UpdateNoteNodeRequest;
import com.uit.buddy.dto.request.note.UpdateNoteRequest;
import com.uit.buddy.dto.response.note.NoteDetailResponse;
import com.uit.buddy.dto.response.note.NoteSummaryResponse;
import com.uit.buddy.dto.response.note.NoteTreeResponse;
import com.uit.buddy.entity.note.Note;
import com.uit.buddy.entity.note.NoteNode;
import com.uit.buddy.exception.note.NoteException;
import com.uit.buddy.repository.note.NoteNodeRepository;
import com.uit.buddy.repository.note.NoteRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NoteServiceImplTest {

    @Mock
    private NoteNodeRepository noteNodeRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private static final String MSSV = "22100001";
    private UUID rootId;
    private UUID childId;
    private UUID noteId;

    @BeforeEach
    void setUp() {
        rootId = UUID.randomUUID();
        childId = UUID.randomUUID();
        noteId = UUID.randomUUID();
    }

    @Test
    void shouldBuildTreeSuccessfully() {
        NoteNode root = createNode(rootId, null, "Root");
        NoteNode child = createNode(childId, rootId, "Child");

        Note categorized = createNote(noteId, childId, "Node Note", LocalDateTime.now().minusHours(1));
        Note uncategorized = createNote(UUID.randomUUID(), null, "Loose Note", LocalDateTime.now());

        when(noteNodeRepository.findByMssvOrderByCreatedAtAsc(MSSV)).thenReturn(List.of(root, child));
        when(noteRepository.findByMssvOrderByUpdatedAtDesc(MSSV)).thenReturn(List.of(categorized, uncategorized));

        NoteTreeResponse result = noteService.getTree(MSSV);

        assertThat(result.nodes()).hasSize(1);
        assertThat(result.nodes().get(0).getId()).isEqualTo(rootId);
        assertThat(result.nodes().get(0).getChildren()).hasSize(1);
        assertThat(result.nodes().get(0).getChildren().get(0).getId()).isEqualTo(childId);
        assertThat(result.nodes().get(0).getChildren().get(0).getNotes()).hasSize(1);
        assertThat(result.uncategorizedNotes()).hasSize(1);
    }

    @Test
    void shouldThrowWhenMoveNodeToItself() {
        NoteNode node = createNode(rootId, null, "Self");
        when(noteNodeRepository.findByIdAndMssv(rootId, MSSV)).thenReturn(Optional.of(node));

        UpdateNoteNodeRequest request = new UpdateNoteNodeRequest("Self", rootId);

        assertThatThrownBy(() -> noteService.updateNode(MSSV, rootId, request)).isInstanceOf(NoteException.class);
        verify(noteNodeRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMoveNodeToDescendant() {
        UUID grandChildId = UUID.randomUUID();

        NoteNode current = createNode(rootId, null, "Current");
        NoteNode parent = createNode(childId, grandChildId, "ParentCandidate");
        NoteNode grandChild = createNode(grandChildId, rootId, "GrandChild");

        when(noteNodeRepository.findByIdAndMssv(rootId, MSSV)).thenReturn(Optional.of(current));
        when(noteNodeRepository.findByIdAndMssv(childId, MSSV)).thenReturn(Optional.of(parent));
        when(noteNodeRepository.findByIdAndMssv(grandChildId, MSSV)).thenReturn(Optional.of(grandChild));

        UpdateNoteNodeRequest request = new UpdateNoteNodeRequest("Current", childId);

        assertThatThrownBy(() -> noteService.updateNode(MSSV, rootId, request)).isInstanceOf(NoteException.class);
        verify(noteNodeRepository, never()).save(any());
    }

    @Test
    void shouldDeleteOwnedNode() {
        NoteNode node = createNode(rootId, null, "Delete me");
        when(noteNodeRepository.findByIdAndMssv(rootId, MSSV)).thenReturn(Optional.of(node));

        noteService.deleteNode(MSSV, rootId);

        verify(noteNodeRepository).delete(node);
    }

    @Test
    void shouldValidateNodeAndTrimKeywordWhenGetNotes() {
        UUID nodeFilterId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        NoteNode node = createNode(nodeFilterId, null, "Filter");
        Note note = createNote(noteId, nodeFilterId, "Java", LocalDateTime.now());
        Page<Note> page = new PageImpl<>(List.of(note), pageable, 1);

        when(noteNodeRepository.findByIdAndMssv(nodeFilterId, MSSV)).thenReturn(Optional.of(node));
        when(noteRepository.searchNotes(MSSV, nodeFilterId, "abc", pageable)).thenReturn(page);

        Page<NoteSummaryResponse> result = noteService.getNotes(MSSV, nodeFilterId, "  abc  ", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(noteRepository).searchNotes(MSSV, nodeFilterId, "abc", pageable);
    }

    @Test
    void shouldCreateNoteSuccessfully() {
        UUID nodeId = UUID.randomUUID();
        NoteNode node = createNode(nodeId, null, "Folder");
        CreateNoteRequest request = new CreateNoteRequest("  New Note  ", "content", nodeId);

        Note saved = createNote(noteId, nodeId, "New Note", LocalDateTime.now());

        when(noteNodeRepository.findByIdAndMssv(nodeId, MSSV)).thenReturn(Optional.of(node));
        when(noteRepository.saveAndFlush(any(Note.class))).thenReturn(saved);

        NoteDetailResponse result = noteService.createNote(MSSV, request);

        assertThat(result.id()).isEqualTo(noteId);
        assertThat(result.title()).isEqualTo("New Note");
    }

    @Test
    void shouldUpdateNoteSuccessfully() {
        UUID targetNodeId = UUID.randomUUID();
        NoteNode targetNode = createNode(targetNodeId, null, "Target");
        Note existing = createNote(noteId, null, "Old", LocalDateTime.now().minusDays(1));

        UpdateNoteRequest request = new UpdateNoteRequest("  Updated  ", "new body", targetNodeId);

        when(noteRepository.findByIdAndMssv(noteId, MSSV)).thenReturn(Optional.of(existing));
        when(noteNodeRepository.findByIdAndMssv(targetNodeId, MSSV)).thenReturn(Optional.of(targetNode));
        when(noteRepository.saveAndFlush(existing)).thenReturn(existing);

        NoteDetailResponse result = noteService.updateNote(MSSV, noteId, request);

        assertThat(existing.getTitle()).isEqualTo("Updated");
        assertThat(existing.getContent()).isEqualTo("new body");
        assertThat(existing.getNodeId()).isEqualTo(targetNodeId);
        assertThat(result.id()).isEqualTo(noteId);
    }

    @Test
    void shouldThrowWhenNoteNotFound() {
        when(noteRepository.findByIdAndMssv(noteId, MSSV)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getNoteDetail(MSSV, noteId)).isInstanceOf(NoteException.class);
    }

    private NoteNode createNode(UUID id, UUID parentId, String name) {
        NoteNode node = NoteNode.builder().mssv(MSSV).name(name).parentId(parentId).build();
        node.setId(id);
        node.setCreatedAt(LocalDateTime.now());
        node.setUpdatedAt(LocalDateTime.now());
        return node;
    }

    private Note createNote(UUID id, UUID nodeId, String title, LocalDateTime updatedAt) {
        Note note = Note.builder().mssv(MSSV).nodeId(nodeId).title(title).content("content").build();
        note.setId(id);
        note.setCreatedAt(updatedAt.minusDays(1));
        note.setUpdatedAt(updatedAt);
        return note;
    }
}
