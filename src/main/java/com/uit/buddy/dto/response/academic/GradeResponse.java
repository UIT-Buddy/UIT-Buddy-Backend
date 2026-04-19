package com.uit.buddy.dto.response.academic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
