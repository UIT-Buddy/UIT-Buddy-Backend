package com.uit.buddy.exception.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DocumentErrorCode {

    FILE_NOT_FOUND("DOC_001", "File not found!", HttpStatus.BAD_REQUEST),
    FOLDER_NOT_FOUND("DOC_002", "Folder not found!", HttpStatus.BAD_REQUEST),
    FOLDER_ALREADY_EXISTS("DOC_003", "Folder name already exists in this location", HttpStatus.BAD_REQUEST),
    FILE_ACCESS_DENIED("DOC_004", "You do not have access to this file", HttpStatus.FORBIDDEN),
    FOLDER_ACCESS_DENIED("DOC_005", "You do not have access to this folder", HttpStatus.FORBIDDEN),
    CANNOT_SHARE_WITH_SELF("DOC_006", "Cannot share resource with yourself", HttpStatus.BAD_REQUEST),
    RECIPIENT_NOT_FOUND("DOC_007", "Recipient student not found", HttpStatus.NOT_FOUND),
    INVALID_SHARE_ROLE("DOC_008", "Invalid share role", HttpStatus.BAD_REQUEST),
    INVALID_SHARE_RESOURCE_TYPE("DOC_009", "Invalid share resource type", HttpStatus.BAD_REQUEST),
    SHARE_NOT_FOUND("DOC_010", "Share not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
