package com.uit.buddy.dto.response.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CometChatUserResponse(@JsonProperty("uid") String uid, @JsonProperty("name") String name,
        @JsonProperty("avatar") String avatar, @JsonProperty("role") String role, @JsonProperty("status") String status,
        @JsonProperty("createdAt") Long createdAt) {
}
