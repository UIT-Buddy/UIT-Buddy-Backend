package com.uit.buddy.dto.response.schedule;

import com.uit.buddy.enums.DeadlineStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CourseContentResponse(String courseName, List<exercise> exercises) {
<<<<<<< HEAD
    public record exercise(@JsonInclude(JsonInclude.Include.NON_NULL) UUID id, String exerciseName,
            LocalDateTime dueDate, @JsonInclude(JsonInclude.Include.NON_NULL) String url, DeadlineStatus status,
=======
    public record exercise(UUID id, String exerciseName, LocalDateTime dueDate, String url, DeadlineStatus status,
>>>>>>> d91f3d347f36f3fb08fa6ee16e85f9cb3c76c5fb
            boolean isPersonal) {
    }
}
