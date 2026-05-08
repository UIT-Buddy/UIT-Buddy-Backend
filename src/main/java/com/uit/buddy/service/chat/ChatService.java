package com.uit.buddy.service.chat;

import com.uit.buddy.dto.request.chat.ChatRequest;
import com.uit.buddy.dto.response.chat.ChatResponse;

public interface ChatService {
    ChatResponse processChat(ChatRequest request);
}
