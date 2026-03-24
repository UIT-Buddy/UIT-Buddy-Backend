package com.uit.buddy.exception.notification;

import com.uit.buddy.exception.BaseException;
import lombok.Getter;

@Getter
public class NotificationException extends BaseException {

    public NotificationException(NotificationErrorCode errorCode) {
        super(errorCode.getCode(), errorCode.getMessage(), errorCode.getHttpStatus());
    }

    public NotificationException(NotificationErrorCode errorCode, String customMessage) {
        super(errorCode.getCode(), customMessage, errorCode.getHttpStatus());
    }
}
