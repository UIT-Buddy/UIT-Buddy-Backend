package com.uit.buddy.service.cometchat.impl;

import com.uit.buddy.client.CometChatClient;
import com.uit.buddy.service.cometchat.CometChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CometChatServiceImpl implements CometChatService {

    private final CometChatClient cometChatClient;

    @Override
    @Async("cometChatExecutor")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void createFriendship(String mssv1, String mssv2) {
        try {
            cometChatClient.addFriend(mssv1, mssv2);
            log.info("[CometChat Service] Friendship created: {} <-> {}", mssv1, mssv2);
        } catch (Exception e) {
            log.error("[CometChat Service] Failed to create friendship: {} <-> {}", mssv1, mssv2, e);
            throw e;
        }
    }

    @Override
    @Async("cometChatExecutor")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void removeFriendship(String mssv1, String mssv2) {
        try {
            cometChatClient.removeFriend(mssv1, mssv2);
            log.info("[CometChat Service] Friendship removed: {} <-> {}", mssv1, mssv2);
        } catch (Exception e) {
            log.error("[CometChat Service] Failed to remove friendship: {} <-> {}", mssv1, mssv2, e);
            throw e;
        }
    }
}
