package com.uit.buddy.exception.fcm;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class FcmException extends BaseException {

    public FcmException(FcmErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public FcmException(FcmErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
