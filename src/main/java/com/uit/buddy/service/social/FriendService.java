package com.uit.buddy.service.social;

import com.uit.buddy.dto.request.social.RespondFriendRequestRequest;
import com.uit.buddy.dto.request.social.SendFriendRequestRequest;
import com.uit.buddy.dto.response.social.FriendRequestResponse;
import com.uit.buddy.dto.response.social.FriendshipResponse;

import java.util.List;
import java.util.UUID;

public interface FriendService {

    void sendFriendRequest(String senderMssv, SendFriendRequestRequest request);

    void respondToFriendRequest(String receiverMssv, UUID requestId, RespondFriendRequestRequest request);

    void unfriend(String mssv, String friendMssv);

    List<FriendRequestResponse> getPendingRequests(String mssv);

    List<FriendRequestResponse> getSentRequests(String mssv);

    List<FriendshipResponse> getFriends(String mssv);

    boolean areFriends(String mssv1, String mssv2);
}
