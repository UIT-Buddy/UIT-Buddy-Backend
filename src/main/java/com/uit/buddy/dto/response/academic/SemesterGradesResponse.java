package com.uit.buddy.dto.response.academic;

import com.uit.buddy.enums.CourseCategoryCode;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SemesterGradesResponse {
    private String semesterCode;
    private Integer totalCredits;
    private Integer accumulatedCredits;
    private Float averageGradeScale10;
    private Float averageGradeScale4;
    private Map<CourseCategoryCode, Integer> totalCreditsByCategory;
    private List<GradeResponse> grades;
}
