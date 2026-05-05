package com.uit.buddy.service.note.impl;

import com.uit.buddy.dto.request.note.UpsertNoteRequest;
import com.uit.buddy.dto.response.note.NoteResponse;
import com.uit.buddy.entity.note.Note;
import com.uit.buddy.repository.note.NoteRepository;
import com.uit.buddy.service.note.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    @Override
    @Transactional(readOnly = true)
    public NoteResponse getNote(String mssv) {
        Note note = noteRepository.findByMssv(mssv).orElse(null);
        if (note == null) {
            return new NoteResponse(mssv, "", null);
        }
        return new NoteResponse(note.getMssv(), note.getContent() != null ? note.getContent() : "",
                note.getUpdatedAt());
    }

    @Override
    @Transactional
    public NoteResponse upsertNote(String mssv, UpsertNoteRequest request) {
        Note note = noteRepository.findByMssv(mssv).orElse(null);

        if (note == null) {
            note = Note.builder().mssv(mssv).content(request.content()).build();
        } else {
            note.setContent(request.content());
        }

        Note saved = noteRepository.saveAndFlush(note);
        return new NoteResponse(saved.getMssv(), saved.getContent() != null ? saved.getContent() : "",
                saved.getUpdatedAt());
    }
}
