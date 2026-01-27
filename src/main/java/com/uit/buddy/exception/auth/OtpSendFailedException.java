package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class OtpSendFailedException extends AuthException {
    public OtpSendFailedException() {
        super(
                AuthErrorCode.OTP_SEND_FAILED,
                "Không thể gửi mã OTP. Vui lòng thử lại sau",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    public OtpSendFailedException(Throwable cause) {
        super(
                AuthErrorCode.OTP_SEND_FAILED,
                "Không thể gửi mã OTP. Vui lòng thử lại sau",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                cause);
    }
}
