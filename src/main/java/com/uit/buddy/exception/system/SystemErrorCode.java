package com.uit.buddy.exception.system;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SystemErrorCode {
    INTERNAL_ERROR("SYS_001", "An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("SYS_002", "Validation failed", HttpStatus.BAD_REQUEST),
    DATABASE_ERROR("SYS_003", "Database error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND("SYS_004", "Resource not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("SYS_005", "HTTP method not supported", HttpStatus.METHOD_NOT_ALLOWED),
    EXTERNAL_SERVICE_ERROR("SYS_006", "External service error", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_PARAMETER("SYS_007", "Invalid parameter provided", HttpStatus.BAD_REQUEST),
    MULTIPART_ERROR("SYS_008", "Invalid multipart request. Please check your file upload.", HttpStatus.BAD_REQUEST),
    UTILITY_CLASS_INSTANTIATION("SYS_009", "Utility class cannot be instantiated", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

}
