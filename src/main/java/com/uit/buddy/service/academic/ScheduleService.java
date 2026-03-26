package com.uit.buddy.service.academic;

import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ScheduleService {
    List<CourseCalendarResponse.Course> uploadSchedule(String mssv, UploadScheduleRequest request);

    DeadlineResponse fetchDeadlinesFromMoodle(String mssv, Integer month, Integer year, Pageable pageable);

    CourseCalendarResponse fetchCourseCalendar(String mssv, String year, String semester);
}
