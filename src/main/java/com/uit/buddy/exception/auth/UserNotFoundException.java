package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AuthException {
    public UserNotFoundException() {
        super(
                AuthErrorCode.USER_NOT_FOUND,
                "Không tìm thấy người dùng",
                HttpStatus.NOT_FOUND.value());
    }

    public UserNotFoundException(String mssv) {
        super(
                AuthErrorCode.USER_NOT_FOUND,
                String.format("Không tìm thấy người dùng với MSSV: %s", mssv),
                HttpStatus.NOT_FOUND.value());
    }
}
