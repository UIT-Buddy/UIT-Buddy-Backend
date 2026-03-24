package com.uit.buddy.dto.request.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserSettingRequest(@NotNull(message = "Enable notification is required") Boolean enableNotification,
        @NotNull(message = "Enable schedule reminder is required") Boolean enableScheduleReminder) {
}
