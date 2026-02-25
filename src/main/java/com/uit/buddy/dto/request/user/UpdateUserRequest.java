package com.uit.buddy.dto.request.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 255, message = "Avatar URL is too long") String avatarUrl,

        @Size(max = 500, message = "Bio must not exceed 500 characters") String bio) {
}