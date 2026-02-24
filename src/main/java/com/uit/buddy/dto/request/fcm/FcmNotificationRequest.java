package com.uit.buddy.dto.request.fcm;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record FcmNotificationRequest(
        @NotBlank String targetToken,

        @NotBlank String id,
        @NotBlank String title,
        @NotBlank String message,
        String image,

        @NotBlank String targetType,

        @NotBlank String targetId) {
    public Map<String, String> toDataMap() {
        return Map.of(
                "id", id,
                "title", title,
                "message", message,
                "image", image != null ? image : "",
                "target_type", targetType,
                "target_id", targetId);
    }
}