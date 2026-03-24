package com.uit.buddy.dto.response.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UnreadCountResponse(@JsonProperty("unread_notification") long unreadNotification) {
}
