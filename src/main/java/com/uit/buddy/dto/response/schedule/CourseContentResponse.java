package com.uit.buddy.dto.response.schedule;

import java.time.LocalDateTime;
import java.util.List;

public record CourseContentResponse(String courseName, List<exercise> exercises) {
    public record exercise(String exerciseName, LocalDateTime dueDate, String url) {
    }
}
