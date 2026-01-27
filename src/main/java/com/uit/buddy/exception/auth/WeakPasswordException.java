package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class WeakPasswordException extends AuthException {
    public WeakPasswordException() {
        super(
                AuthErrorCode.WEAK_PASSWORD,
                "Mật khẩu quá yếu. Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt",
                HttpStatus.BAD_REQUEST.value());
    }

    public WeakPasswordException(String message) {
        super(
                AuthErrorCode.WEAK_PASSWORD,
                message,
                HttpStatus.BAD_REQUEST.value());
    }
}
