package com.uit.buddy.service.academic;

import com.uit.buddy.dto.request.schedule.CreateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UpdateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.CreateDeadlineResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.entity.learning.TemporaryDeadline;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ScheduleService {
    List<CourseCalendarResponse.Course> uploadSchedule(String mssv, UploadScheduleRequest request);

    List<String> fetchStudyingClassCodes(String mssv);

    CreateDeadlineResponse createDeadline(String mssv, CreateDeadlineRequest request);

    CreateDeadlineResponse updateDeadline(String mssv, UpdateDeadlineRequest request);

    DeadlineResponse fetchDeadline(String mssv, Integer month, Integer year, Pageable pageable);

    DeadlineResponse fetchDeadlinesFromMoodle(String mssv, Integer month, Integer year, Pageable pageable);

    CourseCalendarResponse fetchCourseCalendar(String mssv, String year, String semester);

    List<CourseContentResponse> fetchCourseDeadlinesFromMoodle(String mssv, Integer month, Integer year);

    List<TemporaryDeadline> getUpcomingDeadlines(String mssv);

    /**
     * Sync assignment deadlines from Moodle for all enrolled courses. Fetches course contents in parallel, then
     * batch-fetches submission statuses in parallel. Falls back to date-only inference when Moodle is unavailable.
     */
    List<CourseCalendarResponse.Course> syncAssignments(String mssv, Integer month, Integer year);

    /**
     * Sync assignment deadlines for a single course (identified by classId). Fetches course detail and batch-fetches
     * submission statuses for its modules in parallel. Used for lazy-loading deadlines per course.
     */
    CourseContentResponse syncCourseAssignments(String mssv, String classId, Integer month, Integer year);
}
