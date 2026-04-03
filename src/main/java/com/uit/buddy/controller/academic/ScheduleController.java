package com.uit.buddy.controller.academic;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.schedule.CreateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UpdateDeadlineRequest;
import com.uit.buddy.dto.request.schedule.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.CourseContentResponse;
import com.uit.buddy.dto.response.schedule.CreateDeadlineResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.scheduler.ScheduleScheduler;
import com.uit.buddy.service.academic.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule", description = "Schedule management APIs")
public class ScheduleController extends AbstractBaseController {

    private final ScheduleService scheduleService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ICS schedule file", description = "Parse and save student schedule from ICS file. "
            + "After upload, call /api/schedule/assignments/sync to fetch deadlines from Moodle.")
    public ResponseEntity<SingleResponse<List<CourseCalendarResponse.Course>>> uploadSchedule(
            @Valid @ModelAttribute UploadScheduleRequest request, @AuthenticationPrincipal String mssv) {

        if (!request.isIcsFile()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }
        log.info("[POST /api/schedule/upload] Uploading schedule for student: {}", mssv);

        List<CourseCalendarResponse.Course> uploadedCourses = scheduleService.uploadSchedule(mssv, request);

        return successSingle(uploadedCourses, "Schedule uploaded successfully");
    }

    @PostMapping("/assignments/sync")
    @Operation(summary = "Sync assignments from Moodle", description = "Fetch assignment deadlines from Moodle for all enrolled courses. "
            + "Submission status is resolved in parallel for all modules. "
            + "Call this after /upload or at any time to refresh deadlines.")
    public ResponseEntity<SingleResponse<List<CourseCalendarResponse.Course>>> syncAssignmentsFromMoodle(
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year, @AuthenticationPrincipal String mssv) {

        log.info("[POST /api/schedule/assignments/sync] Syncing assignments from Moodle for student: {}", mssv);
        List<CourseCalendarResponse.Course> courses = scheduleService.syncAssignments(mssv, month, year);
        return successSingle(courses, "Assignments synced successfully");
    }

    @PostMapping("/assignments/sync/{classId}")
    @Operation(summary = "Sync assignments for one course", description = "Fetch assignment deadlines from Moodle for a single course (identified by classId). "
            + "Use this to lazily load deadlines per course when the user opens the deadline view.")
    public ResponseEntity<SingleResponse<CourseContentResponse>> syncCourseAssignments(@PathVariable String classId,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year, @AuthenticationPrincipal String mssv) {

        log.info("[POST /api/schedule/assignments/sync/{}] Syncing assignments for classId={}", classId, classId);
        CourseContentResponse deadlines = scheduleService.syncCourseAssignments(mssv, classId, month, year);
        return successSingle(deadlines, "Course assignments synced");
    }

    @PostMapping("/deadline")
    @Operation(summary = "Create deadline", description = "Create personal or course-linked deadline")
    public ResponseEntity<SingleResponse<CreateDeadlineResponse>> createDeadline(
            @Valid @RequestBody CreateDeadlineRequest request, @AuthenticationPrincipal String mssv) {
        log.info("[POST /api/schedule/deadline] Creating deadline for student: {}", mssv);
        CreateDeadlineResponse exercise = scheduleService.createDeadline(mssv, request);
        return successSingle(exercise, "Deadline created successfully");
    }

    @PatchMapping("/deadline")
    @Operation(summary = "Update deadline", description = "Update personal or course-linked deadline")
    public ResponseEntity<SingleResponse<CreateDeadlineResponse>> updateDeadline(
            @Valid @RequestBody UpdateDeadlineRequest request, @AuthenticationPrincipal String mssv) {
        log.info("[PATCH /api/schedule/deadline] Updating deadline {} for student: {}", request.studentTaskId(), mssv);
        CreateDeadlineResponse exercise = scheduleService.updateDeadline(mssv, request);
        return successSingle(exercise, "Deadline updated successfully");
    }

    @GetMapping("/deadline/class-codes/studying")
    @Operation(summary = "Fetch studying class codes", description = "Fetch classCode list for current user where status is STUDYING")
    public ResponseEntity<SingleResponse<List<String>>> fetchStudyingClassCodes(@AuthenticationPrincipal String mssv) {
        log.info("[GET /api/schedule/deadline/class-codes/studying] Fetching studying class codes for student: {}",
                mssv);
        List<String> classCodes = scheduleService.fetchStudyingClassCodes(mssv);
        return successSingle(classCodes, "Studying class codes fetched successfully");
    }

    @GetMapping("/deadline")
    @Operation(summary = "Fetch deadlines", description = "Fetch assignment deadlines from Moodle and StudentTask")
    public ResponseEntity<SingleResponse<DeadlineResponse>> fetchDeadlinesFromMoodle(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year, @AuthenticationPrincipal String mssv) {
        ScheduleScheduler.stopSchedule();
        log.info("[GET /api/schedule/deadline] Fetching deadlines for student: {}", mssv);
        if (month != null && (month < 1 || month > 12)) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_MONTH);
        }
        if (month != null && year == null) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILTER_WITH_DEADLINES);
        }
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        DeadlineResponse deadlines = scheduleService.fetchDeadline(mssv, month, year, pageable);
        return successSingle(deadlines, "Deadlines fetched successfully");
    }

    @GetMapping("/calendar")
    @Operation(summary = "Fetch course calendar", description = "Fetch course schedule for the student")
    public ResponseEntity<SingleResponse<CourseCalendarResponse>> fetchCourseCalendar(
            @RequestParam(name = "year", required = false) String year,
            @RequestParam(name = "semester", required = false) String semester, @AuthenticationPrincipal String mssv) {
        log.info("[GET /api/schedule/calendar] Fetching course calendar for student: {}", mssv);
        if (semester != null && year == null || semester == null && year != null) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILTER_WITH_CALENDAR);
        }
        CourseCalendarResponse calendar = scheduleService.fetchCourseCalendar(mssv, year, semester);

        return successSingle(calendar, "Course calendar fetched successfully");
    }
}
