package com.uit.buddy.dto.response.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        StudentResponse user) {
}
