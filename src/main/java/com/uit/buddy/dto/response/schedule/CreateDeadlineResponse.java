package com.uit.buddy.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uit.buddy.enums.DeadlineStatus;

import java.time.LocalDateTime;

public record CreateDeadlineResponse(
        @JsonInclude(JsonInclude.Include.NON_NULL) String classCode,
        Boolean isPersonal,
        String deadlineName,
        LocalDateTime dueDate,
        DeadlineStatus status
) {
}
