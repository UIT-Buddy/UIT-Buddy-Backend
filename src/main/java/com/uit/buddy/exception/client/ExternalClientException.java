package com.uit.buddy.exception.client;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class ExternalClientException extends BaseException {

    public ExternalClientException(ExternalClientErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public ExternalClientException(ExternalClientErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }

    public ExternalClientException(ExternalClientErrorCode errorCode, Throwable cause) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
        initCause(cause);
    }

    public ExternalClientException(ExternalClientErrorCode errorCode, String customMessage, Throwable cause) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
        initCause(cause);
    }

    public boolean isRateLimited() {
        return getCode().equals(ExternalClientErrorCode.RATE_LIMITED.getCode());
    }
}
