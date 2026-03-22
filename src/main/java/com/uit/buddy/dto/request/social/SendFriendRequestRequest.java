package com.uit.buddy.dto.request.social;

import jakarta.validation.constraints.NotBlank;

public record SendFriendRequestRequest(@NotBlank(message = "Receiver MSSV is required") String receiverMssv) {
}