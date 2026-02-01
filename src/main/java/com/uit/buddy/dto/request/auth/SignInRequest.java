package com.uit.buddy.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

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
public class SignInRequest {

    @NotBlank(message = "mssv is required")
    @Size(min = 8, max = 10, message = "mssv must be 8-10 characters")
    @Pattern(regexp = "^[0-9]+$", message = "mssv must contain only digits")
    private String mssv;

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private Boolean rememberMe = false;
}
