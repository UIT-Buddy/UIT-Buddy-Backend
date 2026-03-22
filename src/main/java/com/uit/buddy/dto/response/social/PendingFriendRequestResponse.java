package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingFriendRequestResponse(
                UUID id,
                UserSummary sender,
                LocalDateTime createdAt) {
}
