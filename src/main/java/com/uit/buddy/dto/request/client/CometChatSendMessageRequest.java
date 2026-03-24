package com.uit.buddy.dto.request.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CometChatSendMessageRequest(
        String receiver,
        String receiverType,
        String category,
        String type,
        Map<String, Object> data) {
}
