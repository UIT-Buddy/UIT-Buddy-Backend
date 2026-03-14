package com.uit.buddy.exception.user;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class UserException extends BaseException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public UserException(UserErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
