package com.uit.buddy.controller.academic;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.request.grade.UploadGradeRequest;
import com.uit.buddy.dto.response.academic.AcademicSummaryResponse;
import com.uit.buddy.dto.response.academic.SemesterGradesResponse;
import com.uit.buddy.exception.grade.*;
import com.uit.buddy.service.academic.GradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grade")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Grade", description = "Grade management APIs")
public class GradeController extends AbstractBaseController {

    private final GradeService gradeService;

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import grade PDF", description = "Upload grade report PDF for the authenticated student")
    public ResponseEntity<SingleResponse<String>> importGradePdf(@Valid @ModelAttribute UploadGradeRequest request,
            @AuthenticationPrincipal String mssv) {

        if (!request.isPdfFile()) {
            throw new GradeException(GradeErrorCode.INVALID_FILE_TYPE);
        }

        log.info("[POST /api/grade/import] Importing grade PDF for student: {}", mssv);
        String result = gradeService.importGradePdf(mssv, request.gradeFile());
        return successSingle(result, "Grade PDF imported successfully");
    }

    @GetMapping("/semester/{semesterCode}")
    @Operation(summary = "Get grades by semester", description = "Get all grades for a student in a specific semester")
    public ResponseEntity<SingleResponse<SemesterGradesResponse>> getGradesBySemester(@PathVariable String semesterCode,
            @AuthenticationPrincipal String mssv) {

        log.info("[GET /api/grade/semester/{}] Getting grades for student: {}", semesterCode, mssv);
        SemesterGradesResponse response = gradeService.getGradesBySemester(mssv, semesterCode);
        return successSingle(response, "Grades retrieved successfully");
    }

    @GetMapping("/all")
    @Operation(summary = "Get all grades", description = "Get cumulative grades for a student across all semesters")
    public ResponseEntity<SingleResponse<List<SemesterGradesResponse>>> getAllGrades(
            @AuthenticationPrincipal String mssv) {

        log.info("[GET /api/grade/all] Getting all grades for student: {}", mssv);
        List<SemesterGradesResponse> response = gradeService.getAllGrades(mssv);
        return successSingle(response, "All grades retrieved successfully");
    }

    @GetMapping("/summary")
    @Operation(summary = "Get academic summary", description = "Get overall academic summary for the authenticated student")
    public ResponseEntity<SingleResponse<AcademicSummaryResponse>> getAcademicSummary(
            @AuthenticationPrincipal String mssv) {

        log.info("[GET /api/grade/summary] Getting academic summary for student: {}", mssv);
        AcademicSummaryResponse response = gradeService.getAcademicSummary(mssv);
        return successSingle(response, "Academic summary retrieved successfully");
    }
}
