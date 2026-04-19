package com.uit.buddy.dto.response.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
