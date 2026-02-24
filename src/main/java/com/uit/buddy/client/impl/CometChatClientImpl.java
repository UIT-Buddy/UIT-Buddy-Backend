package com.uit.buddy.client.impl;

import com.uit.buddy.client.AbstractBaseClient;
import com.uit.buddy.client.CometChatClient;
import com.uit.buddy.dto.request.client.CometChatUserRequest;
import com.uit.buddy.dto.response.client.CometChatUserResponse;
import com.uit.buddy.exception.client.ExternalClientErrorCode;
import com.uit.buddy.exception.client.ExternalClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class CometChatClientImpl extends AbstractBaseClient implements CometChatClient {

    private final String apiKey;
    private final String appId;

    public CometChatClientImpl(
            RestTemplate restTemplate,
            @Value("${app.cometchat.api-url}") String baseUrl,
            @Value("${app.cometchat.api-key}") String apiKey,
            @Value("${app.cometchat.app-id}") String appId) {
        super(restTemplate, baseUrl);
        this.apiKey = apiKey;
        this.appId = appId;
    }

    @Override
    public CometChatUserResponse createUser(CometChatUserRequest request) {
        try {
            log.info("[CometChat] Creating user with uid: {}", request.uid());
            log.debug("[CometChat] Request object type: {}", request.getClass().getName());
            log.debug("[CometChat] Request details - uid: {}, name: {}, avatar: {}",
                    request.uid(), request.name(), request.avatar());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apiKey", apiKey);
            headers.set("appId", appId);

            log.debug("[CometChat] Headers: {}", headers);

            CometChatUserResponse response = post("/users", request, CometChatUserResponse.class, headers);

            if (response == null) {
                log.error("[CometChat] Received null response when creating user");
                throw new ExternalClientException(
                        ExternalClientErrorCode.RESPONSE_PARSING_ERROR,
                        "Failed to create user on CometChat: null response");
            }

            log.info("[CometChat] Successfully created user: {}", response.uid());
            return response;

        } catch (ExternalClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("[CometChat] Unexpected error creating user: {}", e.getMessage(), e);
            throw new ExternalClientException(
                    ExternalClientErrorCode.UNKNOWN_ERROR,
                    "Unexpected error creating user on CometChat",
                    e);
        }
    }

    @Override
    public void deleteUser(String uid) {
        try {
            log.info("[CometChat] Deleting user with uid: {}", uid);

            HttpHeaders headers = new HttpHeaders();
            headers.set("apiKey", apiKey);
            headers.set("appId", appId);

            delete("/users/" + uid, headers);

            log.info("[CometChat] Successfully deleted user: {}", uid);

        } catch (ExternalClientException e) {
            log.warn("[CometChat] Failed to delete user {}: {}", uid, e.getMessage());
        } catch (Exception e) {
            log.warn("[CometChat] Unexpected error deleting user {}: {}", uid, e.getMessage());
        }
    }
}
