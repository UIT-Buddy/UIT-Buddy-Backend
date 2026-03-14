package com.uit.buddy.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgetPasswordRequest(
    @NotBlank(message = "MSSV is required")
        @Pattern(regexp = "^\\d{8,12}$", message = "MSSV must be between 8 and 12 digits")
        String mssv) {}
