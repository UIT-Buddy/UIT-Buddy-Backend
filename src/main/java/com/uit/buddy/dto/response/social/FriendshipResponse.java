package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;
import java.util.UUID;

public record FriendshipResponse(UUID id, UserSummary friend, LocalDateTime createdAt) {
}
