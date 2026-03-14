package com.uit.buddy.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignInRequest(
        @NotBlank(message = "MSSV is required") @Pattern(regexp = "^\\d{8,12}$", message = "MSSV must be between 8 and 12 digits") String mssv,
        @NotBlank(message = "Password is required") String password, Boolean rememberMe, String fcmToken) {
}
