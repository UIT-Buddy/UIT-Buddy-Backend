package com.uit.buddy.event.social;

import java.util.UUID;

public record PostSharedEvent(String actorMssv, String actorName, String receiverMssv, UUID originalPostId,
        UUID sharedPostId) {
}
