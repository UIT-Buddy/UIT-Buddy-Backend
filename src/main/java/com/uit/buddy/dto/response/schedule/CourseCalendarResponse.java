package com.uit.buddy.dto.response.schedule;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
public record CourseCalendarResponse(Integer countOfCourse, String semester, String academicYear,
        List<Course> courses) {
    public record Course(String courseCode, String classId, String courseName, String lecturer, Integer dayOfWeek,
            String startTime, @JsonInclude(JsonInclude.Include.NON_NULL) String labOfClassId,
            boolean isBlendedLearning, String endTime, String startPeriod,
            String endPeriod, String roomCode, String startDate, String endDate, int credits,
            CourseContentResponse deadline) {
    }
}
