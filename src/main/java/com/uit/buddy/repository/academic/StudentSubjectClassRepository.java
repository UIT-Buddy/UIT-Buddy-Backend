package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.StudentSubjectClass;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface StudentSubjectClassRepository extends CrudRepository<StudentSubjectClass, UUID> {
    @Query("SELECT ssc.subjectClass.classCode FROM StudentSubjectClass ssc "
            + "WHERE ssc.student.mssv = :mssv AND ssc.subjectClass.semester.semesterCode = :semesterCode")
    Set<String> findAllClassCodesByStudentAndSemester(@Param("mssv") String mssv,
            @Param("semesterCode") String semesterCode);

    @Query("SELECT ssc FROM StudentSubjectClass ssc " + "JOIN FETCH ssc.subjectClass sc " + "JOIN FETCH sc.course "
            + "WHERE ssc.student.mssv = :mssv AND sc.semester.semesterCode = :semesterCode")
    List<StudentSubjectClass> findAllByStudentMssvAndSemester(@Param("mssv") String mssv,
            @Param("semesterCode") String semesterCode);

    @Query("SELECT ssc FROM StudentSubjectClass ssc " + "JOIN FETCH ssc.subjectClass sc "
            + "WHERE ssc.student.mssv = :mssv AND sc.courseCode = :courseCode ")
    StudentSubjectClass findAllByStudentMssvAndCourseCode(@Param("mssv") String mssv,
            @Param("courseCode") String courseCode);

    @Query("SELECT ssc FROM StudentSubjectClass ssc WHERE ssc.student.mssv = :mssv")
    List<StudentSubjectClass> findSubjectsByMssv(@Param("mssv") String mssv);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudentSubjectClass ssc WHERE ssc.student.mssv = :mssv")
    void deleteAllByMssv(@Param("mssv") String mssv);

    @Query("SELECT ssc FROM StudentSubjectClass ssc WHERE ssc.subjectClass.classCode = :classCode AND ssc.student.mssv = :mssv")
    StudentSubjectClass findSubjectByClassCode(@Param("mssv") String mssv, @Param("classCode") String classCode);

}
