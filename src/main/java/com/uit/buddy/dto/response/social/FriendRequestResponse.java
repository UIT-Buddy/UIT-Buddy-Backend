package com.uit.buddy.dto.response.social;

import com.uit.buddy.enums.FriendRequestStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record FriendRequestResponse(
        UUID id,
        UserSummary sender,
        UserSummary receiver,
        FriendRequestStatus status,
        LocalDateTime createdAt) {
}
