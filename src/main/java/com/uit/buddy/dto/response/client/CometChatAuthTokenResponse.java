package com.uit.buddy.dto.response.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CometChatAuthTokenResponse(AuthTokenData data) {
    public record AuthTokenData(
            String uid,
            @JsonProperty("authToken") String authToken,
            @JsonProperty("createdAt") Long createdAt) {
    }
}
