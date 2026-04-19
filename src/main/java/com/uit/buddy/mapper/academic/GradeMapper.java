package com.uit.buddy.mapper.academic;

import com.uit.buddy.dto.response.academic.GradeResponse;
import com.uit.buddy.dto.response.academic.SemesterGradesResponse;
import com.uit.buddy.entity.academic.Grade;
import com.uit.buddy.entity.academic.Semester;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

    GradeResponse toResponse(Grade grade);

    @Mapping(target = "semesterCode", source = "semester.semesterCode")
    @Mapping(target = "semesterName", source = "semester.semesterCode")
    @Mapping(target = "grades", source = "grades")
    @Mapping(target = "totalCredits", expression = "java(calculateTotalCredits(grades))")
    @Mapping(target = "averageGrade", expression = "java(calculateWeightedAverage(grades))")
    SemesterGradesResponse toSemesterGradesResponse(Semester semester, List<Grade> grades);

    default Integer calculateTotalCredits(List<Grade> grades) {
        return grades.stream().filter(g -> g.getCredits() != null).mapToInt(Grade::getCredits).sum();
    }

    default Float calculateWeightedAverage(List<Grade> grades) {
        double totalWeightedGrade = 0;
        int totalCredits = 0;

        for (Grade grade : grades) {
            if (grade.getTotalGrade() != null && grade.getCredits() != null) {
                totalWeightedGrade += grade.getTotalGrade() * grade.getCredits();
                totalCredits += grade.getCredits();
            }
        }

        return totalCredits > 0 ? (float) (totalWeightedGrade / totalCredits) : null;
    }
}
