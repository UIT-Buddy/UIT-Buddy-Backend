package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class AccountNotActivatedException extends AuthException {
    public AccountNotActivatedException() {
        super(
                AuthErrorCode.ACCOUNT_NOT_ACTIVATED,
                "Tài khoản chưa được kích hoạt. Vui lòng xác thực OTP",
                HttpStatus.FORBIDDEN.value());
    }
}
