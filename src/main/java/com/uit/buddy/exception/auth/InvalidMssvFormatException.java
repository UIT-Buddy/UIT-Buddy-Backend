package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class InvalidMssvFormatException extends AuthException {
    public InvalidMssvFormatException() {
        super(
                AuthErrorCode.INVALID_MSSV_FORMAT,
                "Định dạng MSSV không hợp lệ",
                HttpStatus.BAD_REQUEST.value());
    }

    public InvalidMssvFormatException(String message) {
        super(
                AuthErrorCode.INVALID_MSSV_FORMAT,
                message,
                HttpStatus.BAD_REQUEST.value());
    }
}
