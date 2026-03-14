package com.uit.buddy.exception.homeclass;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class HomeClassException extends BaseException {

    public HomeClassException(HomeClassErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public HomeClassException(HomeClassErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
