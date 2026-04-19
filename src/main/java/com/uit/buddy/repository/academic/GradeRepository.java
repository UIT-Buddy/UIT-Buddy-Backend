package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.Grade;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, UUID> {

    Optional<Grade> findByMssvAndCourseCodeAndSemesterCode(String mssv, String courseCode, String semesterCode);

    List<Grade> findByMssv(String mssv);

    List<Grade> findByMssvAndSemesterCode(String mssv, String semesterCode);

    List<Grade> findBySemesterCode(String semesterCode);

    boolean existsByMssvAndCourseCodeAndSemesterCode(String mssv, String courseCode, String semesterCode);
}
