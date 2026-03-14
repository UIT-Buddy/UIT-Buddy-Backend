package com.uit.buddy.dto.request.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CometChatUserRequest(@JsonProperty("uid") String uid, @JsonProperty("name") String name,
        @JsonProperty("avatar") String avatar) {
}
