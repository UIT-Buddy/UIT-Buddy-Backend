package com.uit.buddy.dto.request.social;

import com.uit.buddy.enums.FriendResponseAction;

import jakarta.validation.constraints.NotBlank;

public record RespondFriendRequestRequest(
        @NotBlank(message = "Action is required") FriendResponseAction action) {
}
