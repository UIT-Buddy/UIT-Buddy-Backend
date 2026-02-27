package com.uit.buddy.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompleteSignUpRequest(
                @NotBlank(message = "Signup token is required") String signupToken,

                @NotBlank(message = "MSSV is required") @Pattern(regexp = "^\\d{8,12}$", message = "MSSV must be between 8 and 12 digits") String mssv,

                @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])\\S{8,}$", message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character") String password,

                @NotBlank(message = "Confirm password is required") String confirmPassword,

                String fcmToken

) {
}
