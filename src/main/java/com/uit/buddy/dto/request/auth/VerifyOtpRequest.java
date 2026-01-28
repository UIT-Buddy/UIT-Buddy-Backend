package com.uit.buddy.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
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
@JsonPropertyOrder({ "mssv", "otp" })
public class VerifyOtpRequest {

    @Schema(description = "Student ID (mssv)", example = "21520001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "mssv is required")
    @Size(min = 8, max = 10, message = "mssv must be 8-10 characters")
    @Pattern(regexp = "^[0-9]+$", message = "mssv must contain only digits")
    private String mssv;

    @Schema(description = "OTP code", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    private String otp;
}
