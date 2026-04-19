package com.uit.buddy.service.academic;

import com.uit.buddy.dto.request.schedule.CreateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UpdateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.CreateDeadlineResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface ScheduleService {
    List<CourseCalendarResponse.Course> uploadSchedule(String mssv, UploadScheduleRequest request);

    List<String> fetchStudyingClassCodes(String mssv);

    CreateDeadlineResponse createDeadline(String mssv, CreateDeadlineRequest request);

    CreateDeadlineResponse updateDeadline(String mssv, UpdateDeadlineRequest request);

    CreateDeadlineResponse getDeadlineDetail(String mssv, UUID deadlineId);

    /**
     * Fetch deadlines for a student. Reads from TemporaryDeadline table (Moodle-sourced) and StudentTask table
     * (personal/course-linked), then merges and paginates.
     */
    DeadlineResponse fetchDeadline(String mssv, Integer month, Integer year, Pageable pageable);

    /**
     * Fetch deadlines from Moodle and sync status into TemporaryDeadline table. Compares fetched deadlines against
     * existing records and updates status where changed.
     */
    CourseCalendarResponse fetchCourseCalendar(String mssv, String year, String semester);

    List<CourseContentResponse> fetchCourseDeadlinesFromMoodle(String mssv, Integer month, Integer year);

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

    /**
     * Syncs all Moodle deadlines for the active semester into the TemporaryDeadline table. Called asynchronously after
     * a user completes signup to pre-populate their deadline cache without blocking the response. Accepts
     * encryptedWstoken to avoid a DB lookup that would fail because the signup transaction has not yet committed.
     */
}
