package com.uit.buddy.dto.response.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SiteInfoResponse(@JsonProperty("userid") Long userid, @JsonProperty("username") String username,
        @JsonProperty("fullname") String fullname) {
}
