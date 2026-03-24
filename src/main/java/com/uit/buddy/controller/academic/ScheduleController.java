package com.uit.buddy.controller.academic;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.dto.response.schedule.DeadlineResponse;
import com.uit.buddy.dto.response.schedule.ScheduleResponse;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.service.academic.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<SingleResponse<List<ScheduleResponse>>> uploadSchedule(
            @Valid @ModelAttribute UploadScheduleRequest request, @AuthenticationPrincipal String mssv) {

        if (!request.isIcsFile()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }
        log.info("[Schedule Controller] Uploading schedule for student: {}", mssv);

        List<ScheduleResponse> schedules = scheduleService.uploadSchedule(mssv, request);

        return successSingle(schedules, "Schedule uploaded successfully");
    }

    @GetMapping("/deadline")
    @Operation(summary = "Fetch deadlines from Moodle", description = "Fetch assignment deadlines from Moodle")
    public ResponseEntity<SingleResponse<DeadlineResponse>> fetchDeadlinesFromMoodle(
            @AuthenticationPrincipal String mssv) {
        log.info("[Schedule Controller] Fetching deadlines from Moodle for student: {}", mssv);

        DeadlineResponse deadlines = scheduleService.fetchDeadlinesFromMoodle(mssv);

        return successSingle(deadlines, "Deadlines fetched successfully");
    }

}
