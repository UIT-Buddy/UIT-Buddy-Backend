package com.uit.buddy.dto.response.academic;

import java.util.List;
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
    private String semesterName;
    private Integer totalCredits;
    private Float averageGrade;
    private List<GradeResponse> grades;
}
