package com.uit.buddy.dto.response.user;

import com.uit.buddy.enums.FriendStatus;

public record UserResponse(String mssv, String fullName, String email, String avatarUrl, String coverUrl, String bio,
        String homeClassCode, FriendStatus friendStatus, Float accumulatedGpaScale10, Float accumulatedGpaScale4,
        Integer accumulatedCredits, Long postCount) {
}
