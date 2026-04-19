package com.uit.buddy.dto.response.academic;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradeResponse {
    private UUID id;
    private String courseCode;
    private String courseName;
    private Integer credits;
    private String courseType;
    private Float processGrade;
    private Float midtermGrade;
    private Float finalGrade;
    private Float labGrade;
    private Float totalGrade;
}
