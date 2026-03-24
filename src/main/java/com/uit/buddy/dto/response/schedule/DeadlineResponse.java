package com.uit.buddy.dto.response.schedule;

import java.util.List;

public record DeadlineResponse(int numberOfDeadlines, List<CourseContentResponse> courseContents) {
}