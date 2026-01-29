package com.uit.buddy.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @Schema(description = "Student ID (mssv)", example = "21520001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "mssv is required")
    private String mssv;

    @Schema(description = "OTP code", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "OTP is required")
    private String otp;

    @Schema(description = "New password", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "New password is required")
    private String newPassword;

    @Schema(description = "Confirm new password", example = "NewPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
