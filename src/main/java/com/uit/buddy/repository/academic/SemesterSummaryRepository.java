package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.SemesterSummary;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterSummaryRepository extends JpaRepository<SemesterSummary, UUID> {
    Optional<SemesterSummary> findByMssvAndSemesterCode(String mssv, String semesterCode);
}
