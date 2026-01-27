package com.uit.buddy.exception.auth;

import org.springframework.http.HttpStatus;

public class TooManyOtpRequestsException extends AuthException {
    public TooManyOtpRequestsException() {
        super(
                AuthErrorCode.TOO_MANY_OTP_REQUESTS,
                "Bạn đã yêu cầu OTP quá nhiều lần. Vui lòng thử lại sau",
                HttpStatus.TOO_MANY_REQUESTS.value());
    }

    public TooManyOtpRequestsException(int waitSeconds) {
        super(
                AuthErrorCode.TOO_MANY_OTP_REQUESTS,
                String.format("Vui lòng đợi %d giây trước khi yêu cầu OTP mới", waitSeconds),
                HttpStatus.TOO_MANY_REQUESTS.value());
    }
}
