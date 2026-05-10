package com.uit.buddy.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatResponse(@JsonProperty("answer") String answer) {
}
