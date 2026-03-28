package com.uit.buddy.repository.learning;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.learning.StudentTask;

@Repository
public interface StudentTaskRepository extends JpaRepository<StudentTask, UUID> {
    @Query("SELECT st FROM StudentTask st LEFT JOIN FETCH st.subjectClass sc LEFT JOIN FETCH sc.course "
            + "WHERE st.mssv = :mssv AND st.reminderAt IS NOT NULL " + "AND MONTH(st.reminderAt) = :month "
            + "AND YEAR(st.reminderAt) = :year")
    List<StudentTask> findDeadlineTasksByMssv(@Param("mssv") String mssv, @Param("month") Integer month,
            @Param("year") Integer year);

    @Query("SELECT st FROM StudentTask st LEFT JOIN FETCH st.subjectClass sc LEFT JOIN FETCH sc.course "
            + "WHERE st.mssv = :mssv AND st.reminderAt IS NOT NULL")
    List<StudentTask> findDeadlineTasksByMssv(@Param("mssv") String mssv);
}
