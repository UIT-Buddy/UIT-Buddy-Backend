package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class OtpExpiredException extends AuthException {
    public OtpExpiredException() {
        super(
                AuthErrorCode.OTP_EXPIRED,
                "Mã OTP đã hết hạn. Vui lòng yêu cầu mã mới",
                HttpStatus.BAD_REQUEST.value());
    }
}
