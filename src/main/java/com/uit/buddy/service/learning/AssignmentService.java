package com.uit.buddy.service.learning;

import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import java.util.List;

public interface AssignmentService {
    List<CourseContentResponse> getDeadlineWithMssv(String mssv, Integer month, Integer year);
}
