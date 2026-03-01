package com.uit.buddy.exception.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode {

    INVALID_PARAMETER("USER_001", "Invalid parameter provided", HttpStatus.BAD_REQUEST),
    STUDENT_NOT_FOUND("USER_002", "Student not found", HttpStatus.NOT_FOUND),
    INVALID_FILE_TYPE("USER_003", "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_TOO_LARGE("USER_004", "File size exceeds maximum limit", HttpStatus.BAD_REQUEST),
    FILE_EMPTY("USER_005", "File cannot be empty", HttpStatus.BAD_REQUEST),
    CAN_NOT_FETCH_USER_PAGE("USER_006", "List of student not found", HttpStatus.BAD_REQUEST);
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
