package com.uit.buddy.repository.learning;

import com.uit.buddy.entity.learning.TemporaryDeadline;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TemporaryDeadlineRepository extends JpaRepository<TemporaryDeadline, UUID> {
    List<TemporaryDeadline> findByMssv(String mssv);

    @Query("SELECT t FROM TemporaryDeadline t WHERE t.dueDate BETWEEN :now AND :threshold")
    List<TemporaryDeadline> findDeadlinesDueBetween(@Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold);

    @Query("SELECT t FROM TemporaryDeadline t WHERE t.mssv = :mssv AND t.dueDate BETWEEN :now AND :threshold ORDER BY t.dueDate ASC")
    List<TemporaryDeadline> findDeadlinesByMssvAndDueBetween(@Param("mssv") String mssv,
            @Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);

    @Query("SELECT t FROM TemporaryDeadline t WHERE t.mssv = :mssv AND t.dueDate BETWEEN :now AND :threshold ORDER BY t.dueDate ASC")
    org.springframework.data.domain.Page<TemporaryDeadline> findDeadlinesByMssvAndDueBetweenPaginated(
            @Param("mssv") String mssv, @Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(t) FROM TemporaryDeadline t WHERE t.mssv = :mssv AND t.dueDate BETWEEN :now AND :threshold")
    long countDeadlinesByMssvAndDueBetween(@Param("mssv") String mssv, @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold);

    @Query("SELECT t FROM TemporaryDeadline t WHERE t.mssv = :mssv AND t.dueDate >= :now")
    org.springframework.data.domain.Page<TemporaryDeadline> findUpcomingDeadlinesPaginated(@Param("mssv") String mssv,
            @Param("now") LocalDateTime now, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(t) FROM TemporaryDeadline t WHERE t.mssv = :mssv AND t.dueDate >= :now")
    long countUpcomingDeadlines(@Param("mssv") String mssv, @Param("now") LocalDateTime now);

    @Query("SELECT t FROM TemporaryDeadline t WHERE t.dueDate <= :now")
    List<TemporaryDeadline> findOverdueDeadlines(@Param("now") LocalDateTime now);

    List<TemporaryDeadline> findByMssvAndSemesterCode(String mssv, String semesterCode);

    @Query("SELECT t FROM TemporaryDeadline t WHERE t.mssv = :mssv AND t.semesterCode = :semesterCode "
            + "AND MONTH(t.dueDate) = :month AND YEAR(t.dueDate) = :year")
    List<TemporaryDeadline> findByMssvAndSemesterCodeAndMonthAndYear(@Param("mssv") String mssv,
            @Param("semesterCode") String semesterCode, @Param("month") int month, @Param("year") int year);

    @Query("SELECT t FROM TemporaryDeadline t WHERE t.mssv = :mssv AND t.semesterCode = :semesterCode")
    List<TemporaryDeadline> findByMssvAndSemesterCodeAll(@Param("mssv") String mssv,
            @Param("semesterCode") String semesterCode);
}
