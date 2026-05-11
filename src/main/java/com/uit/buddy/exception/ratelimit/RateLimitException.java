package com.uit.buddy.exception.ratelimit;

import com.uit.buddy.exception.BaseException;

public class RateLimitException extends BaseException {
    public RateLimitException(RateLimitErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public RateLimitException(RateLimitErrorCode errorCode, String message) {
        super(errorCode.getCode(), message, errorCode.getHttpStatus());
    }
}
