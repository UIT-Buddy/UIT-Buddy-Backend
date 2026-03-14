package com.uit.buddy.dto.response.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MoodleErrorResponse(@JsonProperty("id") String id, @JsonProperty("exception") String exception,
        @JsonProperty("errorcode") String errorcode, @JsonProperty("message") String message) {
}
