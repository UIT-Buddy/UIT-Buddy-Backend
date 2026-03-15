package com.uit.buddy.controller.academic;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.academic.UploadScheduleRequest;
import com.uit.buddy.exception.schedule.ScheduleErrorCode;
import com.uit.buddy.exception.schedule.ScheduleException;
import com.uit.buddy.service.academic.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule", description = "Schedule management APIs")
public class ScheduleController extends AbstractBaseController {

    private final ScheduleService scheduleService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload ICS schedule file", description = "Upload student schedule from ICS file")
    public ResponseEntity<SuccessResponse> uploadSchedule(@Valid @ModelAttribute UploadScheduleRequest request,
            @AuthenticationPrincipal String mssv) {

        if (!request.isIcsFile()) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_FILE_TYPE);
        }
        log.info("[Schedule Controller] Uploading schedule for student: {}", mssv);

        scheduleService.uploadSchedule(mssv, request);

        return success("Schedule uploaded successfully");
    }
}
