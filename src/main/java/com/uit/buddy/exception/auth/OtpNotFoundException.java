package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class OtpNotFoundException extends AuthException {
    public OtpNotFoundException() {
        super(
                AuthErrorCode.OTP_NOT_FOUND,
                "Không tìm thấy mã OTP. Vui lòng yêu cầu mã mới",
                HttpStatus.NOT_FOUND.value());
    }
}
