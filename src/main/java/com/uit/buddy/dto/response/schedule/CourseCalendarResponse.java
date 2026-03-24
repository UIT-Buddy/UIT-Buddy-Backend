package com.uit.buddy.dto.response.schedule;

import java.util.List;

public record CourseCalendarResponse(Integer countOfCourse, List<Course> courses) {
    public record Course(String courseCode, String courseName, Integer dayOfWeek, String startTime, String endTime) {
    }
}
