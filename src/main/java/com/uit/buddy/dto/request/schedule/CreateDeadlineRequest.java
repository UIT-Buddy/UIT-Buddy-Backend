package com.uit.buddy.dto.request.schedule;

import java.time.LocalDateTime;

public record CreateDeadlineRequest(String exerciseName, String classCode, LocalDateTime dueDate) {
}
