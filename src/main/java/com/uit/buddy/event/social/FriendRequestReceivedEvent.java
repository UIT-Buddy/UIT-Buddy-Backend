package com.uit.buddy.event.social;

import java.util.UUID;

public record FriendRequestReceivedEvent(
        UUID requestId,
        String senderMssv,
        String senderName,
        String receiverMssv) {
}
