package com.uit.buddy.exception.auth;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class AuthException extends BaseException {

    public AuthException(AuthErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public AuthException(AuthErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
