package com.uit.buddy.service.note.impl;

import com.uit.buddy.dto.request.note.SaveNoteToDocumentRequest;
import com.uit.buddy.dto.request.note.UpsertNoteRequest;
import com.uit.buddy.dto.response.note.NoteResponse;
import com.uit.buddy.entity.document.Document;
import com.uit.buddy.entity.document.Folder;
import com.uit.buddy.entity.note.Note;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.FileType;
import com.uit.buddy.exception.document.DocumentErrorCode;
import com.uit.buddy.exception.document.DocumentException;
import com.uit.buddy.exception.note.NoteErrorCode;
import com.uit.buddy.exception.note.NoteException;
import com.uit.buddy.repository.document.DocumentRepository;
import com.uit.buddy.repository.document.FolderRepository;
import com.uit.buddy.repository.note.NoteRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.note.NoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final StudentRepository studentRepository;

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

    @Override
    @Transactional
    public UUID saveNoteToDocument(String mssv, SaveNoteToDocumentRequest request) {
        Note note = noteRepository.findByMssv(mssv)
                .orElseThrow(() -> new NoteException(NoteErrorCode.NOTE_NOT_FOUND));

        String content = note.getContent();
        if (content == null || content.isBlank()) {
            throw new NoteException(NoteErrorCode.NOTE_NOT_FOUND);
        }

        Student owner = studentRepository.findById(mssv)
                .orElseThrow(() -> new NoteException(NoteErrorCode.NOTE_NOT_FOUND));

        Folder folder = null;
        if (request.folderId() != null) {
            folder = folderRepository.findById(request.folderId())
                    .orElseThrow(() -> new DocumentException(DocumentErrorCode.FOLDER_NOT_FOUND));

            if (!folder.getMssv().equals(mssv)) {
                throw new DocumentException(DocumentErrorCode.FOLDER_ACCESS_DENIED);
            }
        }

        String fileName = request.fileName();
        if (fileName == null || fileName.isBlank()) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            fileName = "Note_" + timestamp + ".txt";
        }

        Document document = Document.builder()
                .owner(owner)
                .fileName(fileName)
                .fileUrl("")
                .fileType(FileType.WORD)
                .fileSize(0f)
                .folder(folder)
                .content(content)
                .lastEditedBy(mssv)
                .lastEditedAt(Instant.now())
                .build();

        Document saved = documentRepository.save(document);

        log.info("Note saved to document {} by user {}", saved.getId(), mssv);

        return saved.getId();
    }

    @Override
    @Transactional
    public void newNote(String mssv) {
        Note note = noteRepository.findByMssv(mssv).orElse(null);

        if (note == null) {
            note = Note.builder().mssv(mssv).content("").build();
        } else {
            note.setContent("");
        }

        noteRepository.saveAndFlush(note);
    }
}
