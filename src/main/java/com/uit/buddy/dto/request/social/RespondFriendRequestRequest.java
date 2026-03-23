package com.uit.buddy.dto.request.social;

import com.uit.buddy.enums.FriendResponseAction;
import jakarta.validation.constraints.NotNull;

public record RespondFriendRequestRequest(
        @NotNull(message = "Action is required (ACCEPT or REJECT)") FriendResponseAction action) {
}
