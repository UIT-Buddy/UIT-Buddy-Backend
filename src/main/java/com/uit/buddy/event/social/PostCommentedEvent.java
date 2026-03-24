package com.uit.buddy.event.social;

import java.util.UUID;

public record PostCommentedEvent(String actorMssv, String actorName, String receiverMssv, UUID postId, UUID commentId,
        String commentContent) {
}
