package com.uit.buddy.dto.request.schedule;

import com.uit.buddy.enums.DeadlineStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record UpdateDeadlineRequest(String exerciseName, UUID studentTaskId, LocalDateTime dueDate,
        DeadlineStatus status) {
}
