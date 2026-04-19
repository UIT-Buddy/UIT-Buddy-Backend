package com.uit.buddy.service.note.impl;

import com.uit.buddy.dto.request.note.CreateNoteNodeRequest;
import com.uit.buddy.dto.request.note.CreateNoteRequest;
import com.uit.buddy.dto.request.note.UpdateNoteNodeRequest;
import com.uit.buddy.dto.request.note.UpdateNoteRequest;
import com.uit.buddy.dto.response.note.NoteDetailResponse;
import com.uit.buddy.dto.response.note.NoteNodeTreeResponse;
import com.uit.buddy.dto.response.note.NoteSummaryResponse;
import com.uit.buddy.dto.response.note.NoteTreeResponse;
import com.uit.buddy.entity.note.Note;
import com.uit.buddy.entity.note.NoteNode;
import com.uit.buddy.exception.note.NoteErrorCode;
import com.uit.buddy.exception.note.NoteException;
import com.uit.buddy.repository.note.NoteNodeRepository;
import com.uit.buddy.repository.note.NoteRepository;
import com.uit.buddy.service.note.NoteService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteNodeRepository noteNodeRepository;
    private final NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public NoteTreeResponse getTree(String mssv) {
        List<NoteNode> nodes = noteNodeRepository.findByMssvOrderByCreatedAtAsc(mssv);
        List<Note> notes = noteRepository.findByMssvOrderByUpdatedAtDesc(mssv);

        Map<UUID, NoteNodeTreeResponse> mappedNodes = new HashMap<>();
        for (NoteNode node : nodes) {
            mappedNodes.put(node.getId(), toNodeResponse(node));
        }

        List<NoteNodeTreeResponse> rootNodes = new ArrayList<>();
        for (NoteNode node : nodes) {
            NoteNodeTreeResponse current = mappedNodes.get(node.getId());
            if (node.getParentId() == null) {
                rootNodes.add(current);
                continue;
            }

            NoteNodeTreeResponse parent = mappedNodes.get(node.getParentId());
            if (parent == null) {
                rootNodes.add(current);
            } else {
                parent.getChildren().add(current);
            }
        }

        List<NoteSummaryResponse> uncategorizedNotes = new ArrayList<>();
        for (Note note : notes) {
            NoteSummaryResponse summary = toSummaryResponse(note);
            if (note.getNodeId() == null) {
                uncategorizedNotes.add(summary);
                continue;
            }

            NoteNodeTreeResponse ownerNode = mappedNodes.get(note.getNodeId());
            if (ownerNode == null) {
                uncategorizedNotes.add(summary);
            } else {
                ownerNode.getNotes().add(summary);
            }
        }

        sortTree(rootNodes);
        uncategorizedNotes.sort(Comparator.comparing(NoteSummaryResponse::updatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return new NoteTreeResponse(rootNodes, uncategorizedNotes);
    }

    @Override
    @Transactional
    public NoteNodeTreeResponse createNode(String mssv, CreateNoteNodeRequest request) {
        UUID parentId = request.parentId();
        if (parentId != null) {
            getOwnedNode(mssv, parentId);
        }

        NoteNode node = NoteNode.builder()
                .mssv(mssv)
                .name(request.name().trim())
                .parentId(parentId)
                .build();

        NoteNode saved = noteNodeRepository.saveAndFlush(node);
        return toNodeResponse(saved);
    }

    @Override
    @Transactional
    public NoteNodeTreeResponse updateNode(String mssv, UUID nodeId, UpdateNoteNodeRequest request) {
        NoteNode node = getOwnedNode(mssv, nodeId);
        UUID parentId = request.parentId();

        if (parentId != null) {
            validateParent(mssv, node.getId(), parentId);
        }

        node.setName(request.name().trim());
        node.setParentId(parentId);

        NoteNode saved = noteNodeRepository.saveAndFlush(node);
        return toNodeResponse(saved);
    }

    @Override
    @Transactional
    public void deleteNode(String mssv, UUID nodeId) {
        NoteNode node = getOwnedNode(mssv, nodeId);
        noteNodeRepository.delete(node);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoteSummaryResponse> getNotes(String mssv, UUID nodeId, String keyword, Pageable pageable) {
        if (nodeId != null) {
            getOwnedNode(mssv, nodeId);
        }

        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        return noteRepository.searchNotes(mssv, nodeId, normalizedKeyword, pageable)
                .map(this::toSummaryResponse);
    }

    @Override
    @Transactional
    public NoteDetailResponse createNote(String mssv, CreateNoteRequest request) {
        UUID nodeId = request.nodeId();
        if (nodeId != null) {
            getOwnedNode(mssv, nodeId);
        }

        Note note = Note.builder()
                .mssv(mssv)
                .nodeId(nodeId)
                .title(request.title().trim())
                .content(request.content())
                .build();

        Note saved = noteRepository.saveAndFlush(note);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public NoteDetailResponse getNoteDetail(String mssv, UUID noteId) {
        Note note = getOwnedNote(mssv, noteId);
        return toDetailResponse(note);
    }

    @Override
    @Transactional
    public NoteDetailResponse updateNote(String mssv, UUID noteId, UpdateNoteRequest request) {
        Note note = getOwnedNote(mssv, noteId);

        UUID nodeId = request.nodeId();
        if (nodeId != null) {
            getOwnedNode(mssv, nodeId);
        }

        note.setTitle(request.title().trim());
        note.setContent(request.content());
        note.setNodeId(nodeId);

        Note saved = noteRepository.saveAndFlush(note);
        return toDetailResponse(saved);
    }

    @Override
    @Transactional
    public void deleteNote(String mssv, UUID noteId) {
        Note note = getOwnedNote(mssv, noteId);
        noteRepository.delete(note);
    }

    private void sortTree(List<NoteNodeTreeResponse> nodes) {
        nodes.sort(Comparator.comparing(NoteNodeTreeResponse::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())));
        for (NoteNodeTreeResponse node : nodes) {
            node.getNotes().sort(Comparator.comparing(NoteSummaryResponse::updatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            sortTree(node.getChildren());
        }
    }

    private void validateParent(String mssv, UUID currentNodeId, UUID parentId) {
        if (Objects.equals(currentNodeId, parentId)) {
            throw new NoteException(NoteErrorCode.INVALID_NODE_PARENT, "A node cannot be its own parent");
        }

        NoteNode parent = getOwnedNode(mssv, parentId);
        UUID cursor = parent.getParentId();

        while (cursor != null) {
            if (Objects.equals(cursor, currentNodeId)) {
                throw new NoteException(NoteErrorCode.INVALID_NODE_PARENT,
                        "Cannot move node into one of its descendants");
            }
            cursor = noteNodeRepository.findByIdAndMssv(cursor, mssv).map(NoteNode::getParentId).orElse(null);
        }
    }

    private NoteNode getOwnedNode(String mssv, UUID nodeId) {
        return noteNodeRepository.findByIdAndMssv(nodeId, mssv)
                .orElseThrow(() -> new NoteException(NoteErrorCode.NODE_NOT_FOUND));
    }

    private Note getOwnedNote(String mssv, UUID noteId) {
        return noteRepository.findByIdAndMssv(noteId, mssv)
                .orElseThrow(() -> new NoteException(NoteErrorCode.NOTE_NOT_FOUND));
    }

    private NoteNodeTreeResponse toNodeResponse(NoteNode node) {
        return NoteNodeTreeResponse.builder()
                .id(node.getId())
                .parentId(node.getParentId())
                .name(node.getName())
                .updatedAt(node.getUpdatedAt())
                .createdAt(node.getCreatedAt())
                .build();
    }

    private NoteSummaryResponse toSummaryResponse(Note note) {
        return new NoteSummaryResponse(
                note.getId(),
                note.getNodeId(),
                note.getTitle(),
                note.getUpdatedAt(),
                note.getCreatedAt());
    }

    private NoteDetailResponse toDetailResponse(Note note) {
        return new NoteDetailResponse(
                note.getId(),
                note.getNodeId(),
                note.getTitle(),
                note.getContent(),
                note.getUpdatedAt(),
                note.getCreatedAt());
    }
}
