package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.StudentSubjectClass;

import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentSubjectClassRepository extends CrudRepository<StudentSubjectClass, UUID> {
    @Query("SELECT ssc.subjectClass.classCode FROM StudentSubjectClass ssc " +
            "WHERE ssc.student.mssv = :mssv AND ssc.subjectClass.semester.semesterCode = :semesterCode")
    Set<String> findAllClassCodesByStudentAndSemester(String mssv, String semesterCode);
}
