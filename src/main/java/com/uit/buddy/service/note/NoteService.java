package com.uit.buddy.service.note;

import com.uit.buddy.dto.request.note.CreateNoteNodeRequest;
import com.uit.buddy.dto.request.note.CreateNoteRequest;
import com.uit.buddy.dto.request.note.UpdateNoteNodeRequest;
import com.uit.buddy.dto.request.note.UpdateNoteRequest;
import com.uit.buddy.dto.response.note.NoteDetailResponse;
import com.uit.buddy.dto.response.note.NoteNodeTreeResponse;
import com.uit.buddy.dto.response.note.NoteSummaryResponse;
import com.uit.buddy.dto.response.note.NoteTreeResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteService {

    NoteTreeResponse getTree(String mssv);

    NoteNodeTreeResponse createNode(String mssv, CreateNoteNodeRequest request);

    NoteNodeTreeResponse updateNode(String mssv, UUID nodeId, UpdateNoteNodeRequest request);

    void deleteNode(String mssv, UUID nodeId);

    Page<NoteSummaryResponse> getNotes(String mssv, UUID nodeId, String keyword, Pageable pageable);

    NoteDetailResponse createNote(String mssv, CreateNoteRequest request);

    NoteDetailResponse getNoteDetail(String mssv, UUID noteId);

    NoteDetailResponse updateNote(String mssv, UUID noteId, UpdateNoteRequest request);

    void deleteNote(String mssv, UUID noteId);
}
