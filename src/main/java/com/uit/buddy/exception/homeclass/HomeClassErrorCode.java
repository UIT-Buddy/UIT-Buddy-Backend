package com.uit.buddy.exception.homeclass;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HomeClassErrorCode {

    // HomeClass errors
    HOME_CLASS_ALREADY_EXISTS("HC_001", "Home class already exists", HttpStatus.CONFLICT),
    HOME_CLASS_NOT_FOUND("HC_002", "Home class not found", HttpStatus.NOT_FOUND),
    INVALID_HOME_CLASS_FORMAT("HC_003", "Invalid home class code format", HttpStatus.BAD_REQUEST),
    MAJOR_NOT_FOUND("HC_004", "Major not found for home class", HttpStatus.NOT_FOUND),
    INVALID_ACADEMIC_YEAR("HC_005", "Cannot extract academic year from home class code", HttpStatus.BAD_REQUEST),
    HOME_CLASS_CREATION_FAILED("HC_006", "Failed to create home class", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
