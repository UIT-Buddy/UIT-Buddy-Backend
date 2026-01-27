package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class TooManyLoginAttemptsException extends AuthException {
    public TooManyLoginAttemptsException() {
        super(
                AuthErrorCode.TOO_MANY_LOGIN_ATTEMPTS,
                "Bạn đã đăng nhập sai quá nhiều lần. Tài khoản tạm thời bị khóa",
                HttpStatus.TOO_MANY_REQUESTS.value());
    }

    public TooManyLoginAttemptsException(int lockMinutes) {
        super(
                AuthErrorCode.TOO_MANY_LOGIN_ATTEMPTS,
                String.format("Tài khoản bị khóa trong %d phút do đăng nhập sai quá nhiều lần", lockMinutes),
                HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
