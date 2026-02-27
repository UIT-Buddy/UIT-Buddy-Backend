package com.uit.buddy.dto.request.user;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequest(
        @NotBlank(message = "FCM Token cannot be blank") String fcmToken) {
}