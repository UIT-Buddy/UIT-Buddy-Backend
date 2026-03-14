package com.uit.buddy.event.social;

import java.util.UUID;

public record PostLikedEvent(
    String actorMssv, String actorName, String receiverMssv, UUID postId, String content) {}
