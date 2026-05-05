package com.uit.buddy.service.note;

import com.uit.buddy.dto.request.note.UpsertNoteRequest;
import com.uit.buddy.dto.response.note.NoteResponse;

public interface NoteService {

    NoteResponse getNote(String mssv);

    NoteResponse upsertNote(String mssv, UpsertNoteRequest request);
}
