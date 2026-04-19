package com.uit.buddy.service.academic;

import com.uit.buddy.dto.response.academic.SemesterGradesResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GradeService {

    String importGradePdf(String mssv, MultipartFile gradeFile);

    SemesterGradesResponse getGradesBySemester(String mssv, String semesterCode);

    List<SemesterGradesResponse> getAllGrades(String mssv);
}
