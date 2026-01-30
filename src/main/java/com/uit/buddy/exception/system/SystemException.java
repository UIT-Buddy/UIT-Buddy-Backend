package com.uit.buddy.exception.system;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class SystemException extends BaseException {

    public SystemException(SystemErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public SystemException(SystemErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
