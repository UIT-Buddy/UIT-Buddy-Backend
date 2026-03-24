package com.uit.buddy.dto.request.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CometChatPushTokenRequest(@JsonProperty("platform") String platform,
        @JsonProperty("providerId") String providerId, @JsonProperty("fcmToken") String fcmToken,
        @JsonProperty("authToken") String authToken, @JsonProperty("timezone") String timezone) {
}
