package com.uit.buddy.client;

import com.uit.buddy.dto.request.chat.ChatRequest;
import com.uit.buddy.dto.response.chat.ChatResponse;

public interface AIChatClient {
    ChatResponse sendChat(ChatRequest request);
}
