package com.uit.buddy.exception.auth;

public final class AuthErrorCode {

    private AuthErrorCode() {
    }

    // Authentication & Identification
    public static final String INVALID_CREDENTIALS = "AUTH_001";
    public static final String USER_NOT_FOUND = "AUTH_002";
    public static final String ACCOUNT_LOCKED = "AUTH_003";
    public static final String INVALID_PASSWORD = "AUTH_004";
    public static final String EMAIL_NOT_VERIFIED = "AUTH_005";

    // --- UIT Specific ---
    public static final String INVALID_EMAIL_DOMAIN = "AUTH_006"; // Lỗi khi không dùng @gm.uit.edu.vn

    // OTP (Email based)
    public static final String OTP_EXPIRED = "AUTH_100";
    public static final String OTP_INVALID = "AUTH_101";
    public static final String OTP_MAX_ATTEMPTS = "AUTH_102";
    public static final String OTP_GENERATE_FAIL = "AUTH_103";

    // User Management
    public static final String EMAIL_ALREADY_EXISTS = "AUTH_200";
    public static final String INVALID_USER_DATA = "AUTH_201";
    public static final String INVALID_VERIFICATION_TOKEN = "AUTH_202";

    // Security & System
    public static final String TOO_MANY_REQUESTS = "AUTH_300";
    public static final String INTERNAL_AUTH_ERROR = "AUTH_999";
}