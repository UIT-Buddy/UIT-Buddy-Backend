package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class OtpMaxAttemptsException extends AuthException {
    public OtpMaxAttemptsException() {
        super(
                AuthErrorCode.OTP_MAX_ATTEMPTS,
                "Bạn đã nhập sai OTP quá nhiều lần. Vui lòng yêu cầu mã mới",
                HttpStatus.TOO_MANY_REQUESTS.value());
    }

    public OtpMaxAttemptsException(int maxAttempts) {
        super(
                AuthErrorCode.OTP_MAX_ATTEMPTS,
                String.format("Bạn đã nhập sai OTP quá %d lần. Vui lòng yêu cầu mã mới", maxAttempts),
                HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
