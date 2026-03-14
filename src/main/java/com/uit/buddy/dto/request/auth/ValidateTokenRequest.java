package com.uit.buddy.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(@NotBlank(message = "WSToken is required") String wstoken) {
}
