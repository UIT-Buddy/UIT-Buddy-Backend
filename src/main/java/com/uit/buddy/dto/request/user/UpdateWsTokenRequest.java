package com.uit.buddy.dto.request.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateWsTokenRequest(@NotBlank(message = "WSToken is required") String wstoken) {
}
