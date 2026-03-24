package com.uit.buddy.dto.request.user;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(@Size(max = 500, message = "Bio must not exceed 500 characters") String bio) {
}
