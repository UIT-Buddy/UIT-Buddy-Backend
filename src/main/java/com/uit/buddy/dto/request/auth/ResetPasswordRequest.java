package com.uit.buddy.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "MSSV is required") @Pattern(regexp = "^\\d{8,12}$", message = "MSSV must be between 8 and 12 digits") String mssv,
        @NotBlank(message = "OTP code is required") @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits") String otpCode,
        @NotBlank(message = "New password is required") @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#^()\\-+=.])[A-Za-z\\d@$!%*?&_#^()\\-+=.]{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character") String newPassword,
        @NotBlank(message = "Password confirmation is required") String confirmPassword) {
}
