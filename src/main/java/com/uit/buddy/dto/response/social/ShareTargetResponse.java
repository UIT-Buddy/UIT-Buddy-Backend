package com.uit.buddy.dto.response.social;

import com.uit.buddy.enums.ShareTargetType;
import java.time.LocalDateTime;

public record ShareTargetResponse(
        String id,
        String name,
        String avatar,
        ShareTargetType type,
        LocalDateTime lastInteractionAt) {
}
