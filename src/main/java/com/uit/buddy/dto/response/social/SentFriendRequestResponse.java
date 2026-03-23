package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.UUID;

public record SentFriendRequestResponse(UUID id, UserSummary receiver, LocalDateTime createdAt) {
}
