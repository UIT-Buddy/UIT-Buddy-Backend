package com.uit.buddy.dto.response.user;

import com.uit.buddy.enums.FriendStatus;

public record FoundUserResponse(String mssv, String fullName, String avatarUrl, FriendStatus friendStatus) {
}
