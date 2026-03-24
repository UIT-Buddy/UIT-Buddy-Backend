package com.uit.buddy.dto.request.social;

import jakarta.validation.constraints.Size;

public record SharePostRequest(
        @Size(max = 500, message = "Not to much, not exceed 500 characters") String content,
        String receiverId,
        String receiverType) {
}
