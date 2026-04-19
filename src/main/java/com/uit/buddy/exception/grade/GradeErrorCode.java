package com.uit.buddy.exception.grade;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GradeErrorCode {

    STUDENT_NOT_FOUND("GR000", "Student not found", HttpStatus.NOT_FOUND),
    INVALID_OWNER("GR001", "You do not own this grade table", HttpStatus.FORBIDDEN),
    INVALID_FILE_TYPE("GR002", "Only .pdf files are allowed", HttpStatus.BAD_REQUEST),
    INVALID_FILE("GR003", "Empty file or wrong grade table file format", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
