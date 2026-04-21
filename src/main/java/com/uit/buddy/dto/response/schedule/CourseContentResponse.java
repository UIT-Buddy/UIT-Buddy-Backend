package com.uit.buddy.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uit.buddy.enums.DeadlineStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CourseContentResponse(String courseName, List<exercise> exercises) {
    public record exercise(@JsonInclude(JsonInclude.Include.NON_NULL) UUID id, String exerciseName,
            LocalDateTime dueDate, @JsonInclude(JsonInclude.Include.NON_NULL) String url, DeadlineStatus status,
            boolean isPersonal) {
    }
}
