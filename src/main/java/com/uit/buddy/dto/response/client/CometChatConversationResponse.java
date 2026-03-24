package com.uit.buddy.dto.response.client;

import java.util.List;

public record CometChatConversationResponse(List<ConversationData> data) {

    public record ConversationData(String conversationId, String conversationType, ConversationWith conversationWith,
            LastMessage lastMessage) {
    }

    public record ConversationWith(String uid, String guid, String name, String avatar) {
    }

    public record LastMessage(Long sentAt) {
    }
}
