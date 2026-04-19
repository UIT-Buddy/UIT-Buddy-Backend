package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.Semester;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SemesterRepository extends JpaRepository<Semester, String> {

    @Query("SELECT s FROM Semester s WHERE :currentDate BETWEEN s.startDate AND s.endDate")
    Optional<Semester> findCurrentSemester(@Param("currentDate") LocalDate currentDate);

    Optional<Semester> findBySemesterNumberAndYearStartAndYearEnd(String semesterNumber, String yearStart,
            String yearEnd);

}
