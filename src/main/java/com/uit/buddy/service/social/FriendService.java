package com.uit.buddy.service.social;

import com.uit.buddy.dto.request.social.RespondFriendRequestRequest;
import com.uit.buddy.dto.request.social.SendFriendRequestRequest;
import com.uit.buddy.dto.response.social.FriendshipResponse;
import com.uit.buddy.dto.response.social.PendingFriendRequestResponse;
import com.uit.buddy.dto.response.social.SentFriendRequestResponse;
import com.uit.buddy.enums.FriendStatus;
import java.util.List;

public interface FriendService {

    boolean toggleFriendRequest(String senderMssv, SendFriendRequestRequest request);

    void respondToFriendRequest(String senderMssv, String receiverMssv, RespondFriendRequestRequest request);

    void unfriend(String mssv, String friendMssv);

    List<PendingFriendRequestResponse> getPendingRequests(String mssv, String cursor, int limit);

    List<SentFriendRequestResponse> getSentRequests(String mssv, String cursor, int limit);

    List<FriendshipResponse> getFriends(String mssv, String cursor, int limit);

    boolean areFriends(String mssv1, String mssv2);

    FriendStatus getFriendStatus(String currentUserMssv, String targetUserMssv);
}
