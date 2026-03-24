package com.uit.buddy.exception.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode {

    NOTIFICATION_NOT_FOUND("NOTI_001", "Notification not found", HttpStatus.NOT_FOUND),
    FORBIDDEN("NOTI_002", "Access forbidden", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}