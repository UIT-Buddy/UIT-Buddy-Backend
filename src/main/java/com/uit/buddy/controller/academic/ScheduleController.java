package com.uit.buddy.controller.academic;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.CourseCalendarResponse;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.service.academic.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    @Operation(summary = "Upload ICS schedule file", description = "Upload student schedule from ICS file")
    public ResponseEntity<SingleResponse<List<CourseCalendarResponse.Course>>> uploadSchedule(
            @Valid @ModelAttribute UploadScheduleRequest request, @AuthenticationPrincipal String mssv) {

        if (!request.isIcsFile()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }
        log.info("[Schedule Controller] Uploading schedule for student: {}", mssv);

        List<CourseCalendarResponse.Course> uploadedCourses = scheduleService.uploadSchedule(mssv, request);

        return successSingle(uploadedCourses, "Schedule uploaded successfully");
    }

    @GetMapping("/deadline")
    @Operation(summary = "Fetch deadlines from Moodle", description = "Fetch assignment deadlines from Moodle")
    public ResponseEntity<SingleResponse<DeadlineResponse>> fetchDeadlinesFromMoodle(
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "15") int limit,
            @RequestParam(defaultValue = "desc") String sortType,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(name = "month", required = false) Integer month,
            @RequestParam(name = "year", required = false) Integer year, @AuthenticationPrincipal String mssv) {
        log.info("[Schedule Controller] Fetching deadlines from Moodle for student: {}", mssv);
        if (month != null && (month < 1 || month > 12)) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_MONTH);
        }
        if (month != null && year == null) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILTER_WITH_DEADLINES);
        }
        Pageable pageable = createPageable(page, limit, sortType, sortBy);
        DeadlineResponse deadlines = scheduleService.fetchDeadlinesFromMoodle(mssv, month, year, pageable);
        return successSingle(deadlines, "Deadlines fetched successfully");
    }

    @GetMapping("/calendar")
    @Operation(summary = "Fetch course calendar", description = "Fetch course schedule for the student")
    public ResponseEntity<SingleResponse<CourseCalendarResponse>> fetchCourseCalendar(
            @RequestParam(name = "year", required = false) String year,
            @RequestParam(name = "semester", required = false) String semester, @AuthenticationPrincipal String mssv) {
        log.info("[Schedule Controller] Fetching course calendar for student: {}", mssv);
        if (semester != null && year == null || semester == null && year != null) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILTER_WITH_CALENDAR);
        }
        CourseCalendarResponse calendar = scheduleService.fetchCourseCalendar(mssv, year, semester);

        return successSingle(calendar, "Course calendar fetched successfully");
    }
}
