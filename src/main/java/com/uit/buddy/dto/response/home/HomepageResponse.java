package com.uit.buddy.dto.response.home;

import com.uit.buddy.enums.TimeUnit;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HomepageResponse(String studentName, int todayClass, int unreadNotificationCount,
        IncomingClass incomingCourse, int totalDealineCount, List<IncomingDeadline> incomingDeadlines,
        PagingMetadata paging) {
    public record RemainingTime(int unit, TimeUnit unitName) {
    }

    public record IncomingClass(RemainingTime remainingTime, int studentsInClass, String courseCode, String courseName,
            String roomCode, String lecturerName) {
    }

    public record IncomingDeadline(UUID id, String deadlineName, RemainingTime remainingTime, LocalDateTime dueDate) {
    }

    public record PagingMetadata(int currentPage, int totalPages, long totalElements) {
    }

}
