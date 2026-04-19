package com.uit.buddy.exception.note;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoteErrorCode {
    NOTE_NOT_FOUND("NOTE_001", "Note not found", HttpStatus.NOT_FOUND),
    NODE_NOT_FOUND("NOTE_002", "Node not found", HttpStatus.NOT_FOUND),
    INVALID_NODE_PARENT("NOTE_003", "Invalid node parent", HttpStatus.BAD_REQUEST),
    INVALID_NOTE_TITLE("NOTE_004", "Invalid note title", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
