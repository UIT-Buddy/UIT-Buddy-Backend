package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class MssvAlreadyExistsException extends AuthException {
    public MssvAlreadyExistsException(String mssv) {
        super(
                AuthErrorCode.MSSV_ALREADY_EXISTS,
                String.format("MSSV %s đã được đăng ký", mssv),
                HttpStatus.CONFLICT.value());
    }
}
