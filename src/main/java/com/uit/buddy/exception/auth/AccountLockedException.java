package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends AuthException {
    public AccountLockedException() {
        super(
                AuthErrorCode.ACCOUNT_LOCKED,
                "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên",
                HttpStatus.FORBIDDEN.value());
    }

    public AccountLockedException(String reason) {
        super(
                AuthErrorCode.ACCOUNT_LOCKED,
                String.format("Tài khoản đã bị khóa: %s", reason),
                HttpStatus.FORBIDDEN.value());
    }
}
