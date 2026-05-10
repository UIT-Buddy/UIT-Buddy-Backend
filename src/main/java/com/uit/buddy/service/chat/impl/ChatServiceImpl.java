package com.uit.buddy.service.chat.impl;

import com.uit.buddy.client.AIChatClient;
import com.uit.buddy.dto.request.chat.ChatRequest;
import com.uit.buddy.dto.response.chat.ChatResponse;
import com.uit.buddy.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AIChatClient aiChatClient;

    @Override
    public ChatResponse processChat(ChatRequest request) {
        log.info("[Chat Service] Processing chat request: {}", request.getQuestion());
        return aiChatClient.sendChat(request);
    }
}
