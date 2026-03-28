package com.uit.buddy.service.learning;

import java.util.List;

import com.uit.buddy.dto.response.schedule.CourseContentResponse;

public interface AssignmentService {
    List<CourseContentResponse> getDeadlineWithMssv(String mssv, Integer month, Integer year);
}
