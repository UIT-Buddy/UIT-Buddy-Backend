package com.uit.buddy.service.academic;

import com.uit.buddy.dto.response.academic.SemesterGradesResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface GradeService {

    String importGradePdf(String mssv, MultipartFile gradeFile);

    SemesterGradesResponse getGradesBySemester(String mssv, String semesterCode);

    List<SemesterGradesResponse> getAllGrades(String mssv);
}
