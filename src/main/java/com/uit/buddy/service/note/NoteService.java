package com.uit.buddy.service.note;

import com.uit.buddy.dto.request.note.SaveNoteToDocumentRequest;
import com.uit.buddy.dto.request.note.UpsertNoteRequest;
import com.uit.buddy.dto.response.note.NoteResponse;

import java.util.UUID;

public interface NoteService {

    NoteResponse getNote(String mssv);

    NoteResponse upsertNote(String mssv, UpsertNoteRequest request);

    UUID saveNoteToDocument(String mssv, SaveNoteToDocumentRequest request);
}
