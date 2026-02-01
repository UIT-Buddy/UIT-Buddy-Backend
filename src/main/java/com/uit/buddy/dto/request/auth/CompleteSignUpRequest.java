package com.uit.buddy.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteSignUpRequest {

    @NotBlank(message = "mssv is required")
    @Size(min = 8, max = 10, message = "mssv must be 8-10 characters")
    @Pattern(regexp = "^[0-9]+$", message = "mssv must contain only digits")
    private String mssv;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", message = "Password must contain at least one uppercase, one lowercase, one number and one special character (@$!%*?&)")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
