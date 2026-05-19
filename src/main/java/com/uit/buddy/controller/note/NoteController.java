package com.uit.buddy.controller.note;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.note.SaveNoteToDocumentRequest;
import com.uit.buddy.dto.request.note.UpsertNoteRequest;
import com.uit.buddy.dto.response.note.NoteResponse;
import com.uit.buddy.service.note.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/note")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Note", description = "Personal note management API")
public class NoteController extends AbstractBaseController {

    private final NoteService noteService;

    @GetMapping
    @Operation(summary = "Get note", description = "Get personal note for current user")
    public ResponseEntity<SingleResponse<NoteResponse>> getNote(@AuthenticationPrincipal String mssv) {
        NoteResponse response = noteService.getNote(mssv);
        return successSingle(response, "Note retrieved successfully");
    }

    @PutMapping
    @Operation(summary = "Upsert note", description = "Create or update personal note (overwrites existing content)")
    public ResponseEntity<SingleResponse<NoteResponse>> upsertNote(@AuthenticationPrincipal String mssv,
            @Valid @RequestBody UpsertNoteRequest request) {
        NoteResponse response = noteService.upsertNote(mssv, request);
        return successSingle(response, "Note saved successfully");
    }

    @PostMapping("/save-to-document")
    @Operation(summary = "Save note to document", description = "Save current note content as a new document in user's storage")
    public ResponseEntity<SingleResponse<java.util.UUID>> saveNoteToDocument(
            @AuthenticationPrincipal String mssv,
            @RequestBody(required = false) SaveNoteToDocumentRequest request) {
        if (request == null) {
            request = new SaveNoteToDocumentRequest(null, null);
        }
        java.util.UUID documentId = noteService.saveNoteToDocument(mssv, request);
        return successSingle(documentId, "Note saved to document successfully");
    }
}
