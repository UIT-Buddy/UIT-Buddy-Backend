package com.uit.buddy.client;

import com.uit.buddy.dto.request.client.CometChatPushTokenRequest;
import com.uit.buddy.dto.request.client.CometChatSendMessageRequest;
import com.uit.buddy.dto.request.client.CometChatUserRequest;
import com.uit.buddy.dto.response.client.CometChatAuthTokenResponse;
import com.uit.buddy.dto.response.client.CometChatConversationResponse;
import com.uit.buddy.dto.response.client.CometChatGroupResponse;
import com.uit.buddy.dto.response.client.CometChatUserResponse;

public interface CometChatClient {
    CometChatUserResponse createUser(CometChatUserRequest request);

    void deleteUser(String uid);

    void addFriend(String uid1, String uid2);

    void removeFriend(String uid1, String uid2);

    CometChatAuthTokenResponse createCometAuthToken(String uid);

    CometChatUserResponse updateUser(String uid, CometChatUserRequest request);

    void registerPushToken(CometChatPushTokenRequest request);

    void sendMessage(CometChatSendMessageRequest request, String onBehalfOf);

    CometChatGroupResponse getUserGroups(String uid);

    CometChatConversationResponse getConversations(String uid);
}
