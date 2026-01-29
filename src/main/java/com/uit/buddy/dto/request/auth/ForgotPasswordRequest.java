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
public class ForgotPasswordRequest {

    @Schema(description = "Student ID (mssv)", example = "21520001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "mssv is required")
    private String mssv;
}
