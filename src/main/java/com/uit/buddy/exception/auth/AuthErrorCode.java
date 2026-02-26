package com.uit.buddy.exception.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {

    // Authentication errors
    INVALID_CREDENTIALS("AUTH_001", "Invalid MSSV or password", HttpStatus.UNAUTHORIZED),
    INVALID_WSTOKEN("AUTH_002", "Invalid WSToken", HttpStatus.UNAUTHORIZED),
    STUDENT_NOT_FOUND("AUTH_003", "Student not found", HttpStatus.NOT_FOUND),
    STUDENT_ALREADY_EXISTS("AUTH_004", "Student already exists", HttpStatus.CONFLICT),

    // Token errors
    INVALID_TOKEN("AUTH_005", "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REQUIRED("AUTH_006", "Refresh token is required", HttpStatus.BAD_REQUEST),
    REFRESH_TOKEN_EXPIRED("AUTH_007", "Refresh token has expired", HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED("AUTH_008", "Access token has expired", HttpStatus.UNAUTHORIZED),

    // Password errors
    WEAK_PASSWORD("AUTH_009", "Password does not meet security requirements", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("AUTH_010", "Passwords do not match", HttpStatus.BAD_REQUEST),
    PENDING_ACCOUNT_NOT_FOUND("AUTH_011", "MSSV doens't not match WsToken", HttpStatus.NOT_FOUND),

    // OTP errors
    OTP_EXPIRED("AUTH_012", "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID("AUTH_013", "Invalid OTP", HttpStatus.BAD_REQUEST),
    OTP_REQUIRED("AUTH_014", "OTP is required", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED("AUTH_019", "Maximum OTP verification attempts exceeded", HttpStatus.TOO_MANY_REQUESTS),
    EMAIL_SEND_FAILED("AUTH_015", "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),

    // Authorization errors
    UNAUTHORIZED("AUTH_016", "Unauthorized access", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("AUTH_017", "Invalid refresh token", HttpStatus.UNAUTHORIZED),

    // External service errors
    EXTERNAL_SERVICE_ERROR("AUTH_018", "External service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
