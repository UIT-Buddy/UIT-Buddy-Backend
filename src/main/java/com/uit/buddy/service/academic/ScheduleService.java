package com.uit.buddy.service.academic;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.uit.buddy.dto.request.schedule.CreateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.CreateDeadlineResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.entity.learning.TemporaryDeadline;

public interface ScheduleService {
    List<CourseCalendarResponse.Course> uploadSchedule(String mssv, UploadScheduleRequest request);

    List<String> fetchStudyingClassCodes(String mssv);

    CreateDeadlineResponse createDeadline(String mssv, CreateDeadlineRequest request);

    DeadlineResponse fetchDeadline(String mssv, Integer month, Integer year, Pageable pageable);

    DeadlineResponse fetchDeadlinesFromMoodle(String mssv, Integer month, Integer year, Pageable pageable);

    CourseCalendarResponse fetchCourseCalendar(String mssv, String year, String semester);

    List<CourseContentResponse> fetchCourseDeadlinesFromMoodle(String mssv, Integer month, Integer year);

    List<TemporaryDeadline> getUpcomingDeadlines(String mssv);
}
