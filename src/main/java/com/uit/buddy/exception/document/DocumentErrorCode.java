package com.uit.buddy.exception.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DocumentErrorCode {

    FILE_NOT_FOUND("DOC_001", "File not found!", HttpStatus.BAD_REQUEST),
    FOLDER_NOT_FOUND("DOC_002", "Folder not found!", HttpStatus.BAD_REQUEST),
    FOLDER_ALREADY_EXISTS("DOC_003", "Folder name already exists in this location", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
