package com.uit.buddy.dto.response.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicSummaryResponse {
    private Integer attemptedCredits;
    private Integer accumulatedCredits;
    private Float attemptedGpa;
    private Float accumulatedGpa;
    private Float majorProgress;
    private Integer accumulatedGeneralCredits;
    private Integer accumulatedFoundationCredits;
    private Integer accumulatedMajorCredits;
    private Integer accumulatedElectiveCredits;
    private Integer accumulatedGraduationCredits;
}
