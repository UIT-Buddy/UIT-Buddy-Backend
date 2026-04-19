package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.AcademicSummary;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicSummaryRepository extends JpaRepository<AcademicSummary, UUID> {
    Optional<AcademicSummary> findByMssv(String mssv);
}
