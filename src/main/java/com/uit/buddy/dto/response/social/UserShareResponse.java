package com.uit.buddy.dto.response.social;

import java.time.LocalDateTime;

public record UserShareResponse(UserSummary user, LocalDateTime sharedAt) {}
