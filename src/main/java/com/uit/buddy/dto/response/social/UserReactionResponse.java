package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;

public record UserReactionResponse(UserSummary user, LocalDateTime reactedAt) {
}
