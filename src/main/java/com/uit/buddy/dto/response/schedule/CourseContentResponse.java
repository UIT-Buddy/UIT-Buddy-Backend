package com.uit.buddy.dto.response.schedule;

import com.uit.buddy.enums.DeadlineStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CourseContentResponse(String courseName, List<exercise> exercises) {
    public record exercise(UUID id, String exerciseName, LocalDateTime dueDate, String url, DeadlineStatus status,
            boolean isPersonal) {
    }
}
