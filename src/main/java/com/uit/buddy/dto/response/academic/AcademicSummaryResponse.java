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
    private Float attemptedGpaScale10;
    private Float attemptedGpaScale4;
    private Float accumulatedGpaScale10;
    private Float accumulatedGpaScale4;
    private Float majorProgress;
    private Integer accumulatedGeneralCredits;
    private Integer accumulatedPoliticalCredits;
    private Integer accumulatedFoundationCredits;
    private Integer accumulatedMajorCredits;
    private Integer accumulatedElectiveCredits;
    private Integer accumulatedGraduationCredits;
}
