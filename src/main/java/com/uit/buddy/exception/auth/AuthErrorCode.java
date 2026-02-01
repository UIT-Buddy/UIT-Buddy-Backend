package com.uit.buddy.exception.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {

        // Authentication & Identification
        INVALID_CREDENTIALS("AUTH_001", "Invalid email or password", HttpStatus.UNAUTHORIZED),
        USER_NOT_FOUND("AUTH_002", "User not found", HttpStatus.NOT_FOUND),
        ACCOUNT_NOT_ACTIVATED("AUTH_005", "Account is not activated. Please verify your email", HttpStatus.FORBIDDEN),
        MSSV_NOT_FOUND("AUTH_006", "mssv not found", HttpStatus.NOT_FOUND),
        INVALID_MSSV_FORMAT("AUTH_007", "Invalid mssv format. mssv must be 8-10 digits", HttpStatus.BAD_REQUEST),
        REFRESH_TOKEN_REQUIRED("AUTH_008", "Refresh token is required", HttpStatus.BAD_REQUEST),

        // OTP (Email based)
        OTP_EXPIRED("AUTH_100", "OTP has expired. Please request a new one", HttpStatus.BAD_REQUEST),
        OTP_INVALID("AUTH_101", "Invalid OTP", HttpStatus.BAD_REQUEST),
        OTP_MAX_ATTEMPTS("AUTH_102", "Maximum OTP attempts exceeded. Please request a new OTP",
                        HttpStatus.TOO_MANY_REQUESTS),
        OTP_SEND_FAILED("AUTH_103", "Failed to send OTP email", HttpStatus.INTERNAL_SERVER_ERROR),
        OTP_NOT_FOUND("AUTH_104", "OTP not found. Please request a new one", HttpStatus.BAD_REQUEST),

        // Registration & Signup
        MSSV_ALREADY_EXISTS("AUTH_200", "mssv is already registered", HttpStatus.CONFLICT),
        EMAIL_ALREADY_EXISTS("AUTH_201", "Email is already registered", HttpStatus.CONFLICT),
        WEAK_PASSWORD("AUTH_203",
                        "Password must be 8-50 characters with uppercase, lowercase, number, and special character (@$!%*?&)",
                        HttpStatus.BAD_REQUEST),
        PASSWORD_MISMATCH("AUTH_204", "Confirm password does not match", HttpStatus.BAD_REQUEST),
        PENDING_ACCOUNT_NOT_FOUND("AUTH_205", "Pending account not found", HttpStatus.NOT_FOUND),
        PENDING_ACCOUNT_EXPIRED("AUTH_206", "Pending account has expired", HttpStatus.GONE),

        // Token & Session
        TOKEN_EXPIRED("AUTH_300", "Token has expired", HttpStatus.UNAUTHORIZED),
        TOKEN_INVALID("AUTH_301", "Invalid token", HttpStatus.UNAUTHORIZED),
        TOKEN_MISSING("AUTH_302", "Token is required", HttpStatus.UNAUTHORIZED),
        REFRESH_TOKEN_INVALID("AUTH_303", "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),
        REFRESH_TOKEN_NOT_FOUND("AUTH_304", "Refresh token not found", HttpStatus.NOT_FOUND),
        REFRESH_TOKEN_EXPIRED("AUTH_305", "Refresh token has expired", HttpStatus.UNAUTHORIZED),
        SUSPICIOUS_DETECTED("AUTH_306", "Suspicious activity detected. All tokens have been revoked",
                        HttpStatus.UNAUTHORIZED),

        // Security & Rate Limiting
        TOO_MANY_REQUESTS("AUTH_400", "Too many requests. Please try again later", HttpStatus.TOO_MANY_REQUESTS),
        TOO_MANY_OTP_REQUESTS("AUTH_401", "Too many OTP requests. Please wait before requesting again",
                        HttpStatus.TOO_MANY_REQUESTS),

        // System
        INTERNAL_AUTH_ERROR("AUTH_999", "Internal authentication error", HttpStatus.INTERNAL_SERVER_ERROR);

        private final String code;
        private final String message;
        private final HttpStatus httpStatus;
}