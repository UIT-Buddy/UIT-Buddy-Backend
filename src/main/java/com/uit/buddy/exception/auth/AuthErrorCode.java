package com.uit.buddy.exception.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode {

    // Authentication & Identification
    INVALID_CREDENTIALS("AUTH_001", "Invalid email or password", 401),
    USER_NOT_FOUND("AUTH_002", "User not found", 404),
    ACCOUNT_NOT_ACTIVATED("AUTH_005", "Account is not activated. Please verify your email", 403),
    MSSV_NOT_FOUND("AUTH_006", "mssv not found", 404),
    INVALID_MSSV_FORMAT("AUTH_007", "Invalid mssv format. mssv must be 8-10 digits", 400),

    // OTP (Email based)
    OTP_EXPIRED("AUTH_100", "OTP has expired. Please request a new one", 400),
    OTP_INVALID("AUTH_101", "Invalid OTP", 400),
    OTP_MAX_ATTEMPTS("AUTH_102", "Maximum OTP attempts exceeded. Please request a new OTP", 429),
    OTP_SEND_FAILED("AUTH_103", "Failed to send OTP email", 500),
    OTP_NOT_FOUND("AUTH_104", "OTP not found. Please request a new one", 400),

    // Registration & Signup
    MSSV_ALREADY_EXISTS("AUTH_200", "mssv is already registered", 409),
    EMAIL_ALREADY_EXISTS("AUTH_201", "Email is already registered", 409),
    WEAK_PASSWORD("AUTH_203",
            "Password must be 8-50 characters with uppercase, lowercase, number, and special character (@$!%*?&)", 400),
    PASSWORD_MISMATCH("AUTH_204", "Confirm password does not match", 400),

    // Token & Session
    TOKEN_EXPIRED("AUTH_300", "Token has expired", 401),
    TOKEN_INVALID("AUTH_301", "Invalid token", 401),
    TOKEN_MISSING("AUTH_302", "Token is required", 401),
    REFRESH_TOKEN_INVALID("AUTH_303", "Invalid or expired refresh token", 401),
    TEMP_TOKEN_INVALID("AUTH_304", "Invalid or expired temporary token", 400),
    REFRESH_TOKEN_NOT_FOUND("AUTH_305", "Refresh token not found", 404),
    REFRESH_TOKEN_EXPIRED("AUTH_306", "Refresh token has expired", 401),
    SUSPICIOUS_DETECTED("AUTH_307", "Suspicious activity detected. All tokens have been revoked", 401),

    // Security & Rate Limiting
    TOO_MANY_REQUESTS("AUTH_400", "Too many requests. Please try again later", 429),
    TOO_MANY_OTP_REQUESTS("AUTH_401", "Too many OTP requests. Please wait before requesting again", 429),

    // System
    INTERNAL_AUTH_ERROR("AUTH_999", "Internal authentication error", 500);

    private final String code;
    private final String message;
    private final int httpStatus;
}