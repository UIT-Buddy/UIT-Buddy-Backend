package com.uit.buddy.client;

import com.uit.buddy.dto.request.client.CometChatUserRequest;
import com.uit.buddy.dto.response.client.CometChatAuthTokenResponse;
import com.uit.buddy.dto.response.client.CometChatUserResponse;

public interface CometChatClient {
    CometChatUserResponse createUser(CometChatUserRequest request);

    void deleteUser(String uid);

    void addFriend(String uid1, String uid2);

    void removeFriend(String uid1, String uid2);

    CometChatAuthTokenResponse createCometAuthToken(String uid);

    CometChatUserResponse updateUser(String uid, CometChatUserRequest request);
}
