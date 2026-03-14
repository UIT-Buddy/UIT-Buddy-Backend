package com.uit.buddy.client;

import com.uit.buddy.dto.request.client.CometChatUserRequest;
import com.uit.buddy.dto.response.client.CometChatUserResponse;

public interface CometChatClient {
  CometChatUserResponse createUser(CometChatUserRequest request);

  void deleteUser(String uid);
}
