package com.uit.buddy.dto.response.schedule;

import java.util.List;

public record CourseCalendarResponse(Integer countOfCourse, String semester, String academicYear,
        List<Course> courses) {
    public record Course(String courseCode, String courseName, String lecturer, Integer dayOfWeek, String startTime,
            String endTime, String startLesson, String endLesson, String roomCode, String startDate, String endDate) {
    }
}
