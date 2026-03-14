package com.uit.buddy.dto.response.user;

public record UserResponse(String mssv, String fullName, String email, String avatarUrl, String bio,
        String homeClassCode, String cometUid) {
}
