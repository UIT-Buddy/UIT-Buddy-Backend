package com.uit.buddy.dto.response.user;

import com.uit.buddy.enums.FriendStatus;

public record UserResponse(String mssv, String fullName, String email, String avatarUrl, String bio,
        String homeClassCode, FriendStatus friendStatus) {
}
