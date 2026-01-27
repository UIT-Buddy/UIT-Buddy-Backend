package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class OtpInvalidException extends AuthException {
    public OtpInvalidException() {
        super(
                AuthErrorCode.OTP_INVALID,
                "Mã OTP không chính xác",
                HttpStatus.BAD_REQUEST.value());
    }

    public OtpInvalidException(String message) {
        super(
                AuthErrorCode.OTP_INVALID,
                message,
                HttpStatus.BAD_REQUEST.value());
    }
}
