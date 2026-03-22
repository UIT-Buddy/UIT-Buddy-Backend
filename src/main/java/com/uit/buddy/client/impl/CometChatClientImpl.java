package com.uit.buddy.client.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.client.CometChatClient;
import com.uit.buddy.constant.CometChatApiConstants;
import com.uit.buddy.dto.request.client.CometChatUserRequest;
import com.uit.buddy.dto.response.client.CometChatUserResponse;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
public class CometChatClientImpl extends AbstractBaseClient implements CometChatClient {

    private final String apiKey;
    private final String appId;

    public CometChatClientImpl(@Qualifier("cometChatClient") RestClient restClient, ObjectMapper objectMapper,
            @Value("${app.cometchat.api-key}") String apiKey, @Value("${app.cometchat.app-id}") String appId) {
        super(restClient, objectMapper);
        this.apiKey = apiKey;
        this.appId = appId;
    }

    @Override
    public CometChatUserResponse createUser(CometChatUserRequest request) {
        CometChatUserResponse response = post(CometChatApiConstants.USERS_ENDPOINT, request,
                CometChatUserResponse.class, createHeaders());
        return response;
    }

    @Override
    public void addFriend(String uid, String friendUid) {
        String endpoint = String.format(CometChatApiConstants.ADD_FRIENDS_ENDPOINT, uid);
        Map<String, Object> body = Map.of("accepted", List.of(friendUid));
        post(endpoint, body, Object.class, createHeaders());
    }

    @Override
    public void removeFriend(String uid, String friendUid) {
        String endpoint = String.format(CometChatApiConstants.REMOVE_FRIEND_ENDPOINT, uid, friendUid);
        delete(endpoint, createHeaders());
    }

    @Override
    public void deleteUser(String uid) {
        String endpoint = String.format(CometChatApiConstants.USER_BY_UID_ENDPOINT, uid);
        delete(endpoint, createHeaders());
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(CometChatApiConstants.API_KEY_HEADER, apiKey);
        headers.set(CometChatApiConstants.APP_ID_HEADER, appId);
        return headers;
    }
}
