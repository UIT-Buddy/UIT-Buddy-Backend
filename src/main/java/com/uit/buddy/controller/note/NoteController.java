package com.uit.buddy.controller.note;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.PageResponse;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.note.CreateNoteNodeRequest;
import com.uit.buddy.dto.request.note.CreateNoteRequest;
import com.uit.buddy.dto.request.note.UpdateNoteNodeRequest;
import com.uit.buddy.dto.request.note.UpdateNoteRequest;
import com.uit.buddy.dto.response.note.NoteDetailResponse;
import com.uit.buddy.dto.response.note.NoteNodeTreeResponse;
import com.uit.buddy.dto.response.note.NoteSummaryResponse;
import com.uit.buddy.dto.response.note.NoteTreeResponse;
import com.uit.buddy.service.note.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Note", description = "Personal note management APIs")
public class NoteController extends AbstractBaseController {

    private final NoteService noteService;

    @GetMapping("/tree")
    @Operation(summary = "Get note tree", description = "Get hierarchical nodes and notes for current user")
    public ResponseEntity<SingleResponse<NoteTreeResponse>> getTree(@AuthenticationPrincipal String mssv) {
        NoteTreeResponse response = noteService.getTree(mssv);
        return successSingle(response, "Note tree retrieved successfully");
    }

    @PostMapping("/nodes")
    @Operation(summary = "Create note node", description = "Create a new node in personal note tree")
    public ResponseEntity<SingleResponse<NoteNodeTreeResponse>> createNode(
            @AuthenticationPrincipal String mssv,
            @Valid @RequestBody CreateNoteNodeRequest request) {
        NoteNodeTreeResponse response = noteService.createNode(mssv, request);
        return successSingle(response, "Note node created successfully");
    }

    @PatchMapping("/nodes/{nodeId}")
    @Operation(summary = "Update note node", description = "Rename node or move node to another parent")
    public ResponseEntity<SingleResponse<NoteNodeTreeResponse>> updateNode(
            @AuthenticationPrincipal String mssv,
            @PathVariable UUID nodeId,
            @Valid @RequestBody UpdateNoteNodeRequest request) {
        NoteNodeTreeResponse response = noteService.updateNode(mssv, nodeId, request);
        return successSingle(response, "Note node updated successfully");
    }

    @DeleteMapping("/nodes/{nodeId}")
    @Operation(summary = "Delete note node", description = "Delete node and all descendant nodes")
    public ResponseEntity<SuccessResponse> deleteNode(
            @AuthenticationPrincipal String mssv,
            @PathVariable UUID nodeId) {
        noteService.deleteNode(mssv, nodeId);
        return success("Note node deleted successfully");
    }

    @GetMapping
    @Operation(summary = "List notes", description = "List notes with optional node and keyword filters")
    public ResponseEntity<PageResponse<NoteSummaryResponse>> getNotes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(required = false) UUID nodeId,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal String mssv) {
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        Page<NoteSummaryResponse> response = noteService.getNotes(mssv, nodeId, keyword, pageable);
        return paging(response, "Notes retrieved successfully");
    }

    @PostMapping
    @Operation(summary = "Create note", description = "Create a personal note")
    public ResponseEntity<SingleResponse<NoteDetailResponse>> createNote(
            @AuthenticationPrincipal String mssv,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteDetailResponse response = noteService.createNote(mssv, request);
        return successSingle(response, "Note created successfully");
    }

    @GetMapping("/{noteId}")
    @Operation(summary = "Get note detail", description = "Get detail of a note")
    public ResponseEntity<SingleResponse<NoteDetailResponse>> getNoteDetail(
            @AuthenticationPrincipal String mssv,
            @PathVariable UUID noteId) {
        NoteDetailResponse response = noteService.getNoteDetail(mssv, noteId);
        return successSingle(response, "Note detail retrieved successfully");
    }

    @PatchMapping("/{noteId}")
    @Operation(summary = "Update note", description = "Update title/content/node of a note")
    public ResponseEntity<SingleResponse<NoteDetailResponse>> updateNote(
            @AuthenticationPrincipal String mssv,
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        NoteDetailResponse response = noteService.updateNote(mssv, noteId, request);
        return successSingle(response, "Note updated successfully");
    }

    @DeleteMapping("/{noteId}")
    @Operation(summary = "Delete note", description = "Delete a note")
    public ResponseEntity<SuccessResponse> deleteNote(
            @AuthenticationPrincipal String mssv,
            @PathVariable UUID noteId) {
        noteService.deleteNote(mssv, noteId);
        return success("Note deleted successfully");
    }
}
