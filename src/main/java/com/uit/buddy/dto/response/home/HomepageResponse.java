package com.uit.buddy.dto.response.home;

import java.time.LocalDateTime;
import java.util.List;

public record HomepageResponse(String studentName, int todayClass, int unreadNotificationCount,
        IncomingClass incomingCourse, int totalDealineCount, List<IncomingDeadline> incomingDeadlines,
        PagingMetadata paging) {
    public record IncomingClass(int remainingTime, int studentsInClass, String courseCode, String courseName,
            String roomCode, String lecturerName) {
    }

    public record IncomingDeadline(String deadlineName, int remainingTime, LocalDateTime dueDate) {
    }

    public record PagingMetadata(int currentPage, int totalPages, long totalElements) {
    }

}
