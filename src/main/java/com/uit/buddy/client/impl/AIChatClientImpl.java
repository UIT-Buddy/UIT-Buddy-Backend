package com.uit.buddy.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.client.AIChatClient;
import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.dto.request.chat.ChatRequest;
import com.uit.buddy.dto.response.chat.ChatResponse;
import com.uit.buddy.exception.user.UserErrorCode;
import com.uit.buddy.exception.user.UserException;
import com.uit.buddy.security.JwtAuthenticationFilter;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class AIChatClientImpl extends AbstractBaseClient implements AIChatClient {

    private final String apiUrl;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public AIChatClientImpl(RestClient restClient, ObjectMapper objectMapper,
            JwtAuthenticationFilter jwtAuthenticationFilter, @Value("${ai.chat.api-url}") String apiUrl) {
        super(restClient, objectMapper);
        this.apiUrl = apiUrl;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Override
    public ChatResponse sendChat(ChatRequest request) {
        String jwt = jwtAuthenticationFilter.getUserJwt();
        if (jwt == null) {
            throw new UserException(UserErrorCode.AI_SERVICE_UNAVAILABLE);
        }
        Map<String, String> requestMessage = new HashMap<>();
        requestMessage.put("question", request.getQuestion());
        requestMessage.put("authentication", jwt);
        ChatResponse response = post(apiUrl, requestMessage, ChatResponse.class, null);
        return response;
    }
}
