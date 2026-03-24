package com.uit.buddy.event.social;

import java.util.UUID;

public record CommentLikedEvent(String actorMssv, String actorName, String receiverMssv, UUID commentId, UUID postId) {
}
