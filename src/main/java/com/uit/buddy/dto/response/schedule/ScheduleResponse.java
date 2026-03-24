package com.uit.buddy.dto.response.schedule;

import java.time.LocalTime;

public record ScheduleResponse(
        String classCode,
        String courseName,
        String teacherName,
        Integer dayOfWeek,
        Integer startLesson,
        Integer endLesson,
        LocalTime startTime,
        LocalTime endTime,
        String roomCode,
        Integer interval,
        String classType) {
}
