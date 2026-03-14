package com.uit.buddy.exception.fcm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FcmErrorCode {

  // FCM token errors
  INVALID_FCM_TOKEN("FCM_001", "Invalid FCM token", HttpStatus.BAD_REQUEST),
  // Notification errors
  NOTIFICATION_SEND_FAILED(
      "FCM_002", "Failed to send notification", HttpStatus.INTERNAL_SERVER_ERROR),
  INVALID_NOTIFICATION_DATA("FCM_003", "Invalid notification data", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
