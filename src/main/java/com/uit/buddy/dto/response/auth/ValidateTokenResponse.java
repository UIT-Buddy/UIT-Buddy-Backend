package com.uit.buddy.dto.response.auth;

public record ValidateTokenResponse(String signupToken, String mssv, String fullName) {
}
