package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super(
                AuthErrorCode.INVALID_CREDENTIALS,
                "MSSV hoặc mật khẩu không chính xác",
                HttpStatus.UNAUTHORIZED.value());
    }

    public InvalidCredentialsException(String message) {
        super(
                AuthErrorCode.INVALID_CREDENTIALS,
                message,
                HttpStatus.UNAUTHORIZED.value());
    }
}
