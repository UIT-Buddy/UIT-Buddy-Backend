package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.Semester;
import com.uit.buddy.entity.academic.SubjectClass;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectClassRepository extends JpaRepository<SubjectClass, UUID> {
    List<SubjectClass> findAllByClassCodeInAndSemester(Collection<String> classCodes, Semester semester);

    @Query("""
        SELECT ssc.subjectClass
        FROM StudentSubjectClass ssc
        WHERE ssc.subjectClass.classCode = :classCode
          AND ssc.student.mssv = :mssv
    """)
    SubjectClass findByClassCodeAndStudentMssv(
            @Param("mssv") String mssv,
            @Param("classCode") String classCode
    );
}
