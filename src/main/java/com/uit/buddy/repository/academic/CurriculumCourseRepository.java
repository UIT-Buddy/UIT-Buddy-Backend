package com.uit.buddy.repository.academic;

import com.uit.buddy.entity.academic.CurriculumCourse;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CurriculumCourseRepository extends JpaRepository<CurriculumCourse, UUID> {
    @Query(value = "SELECT cc.theory_credits FROM curriculum_courses cc JOIN curriculums c ON c.curriculum_code = cc.curriculum_code WHERE cc.course_code = :courseCode AND c.major_code = :majorCode AND academic_start_year = :year", nativeQuery = true)
    Integer findCreditsForClass(@Param("courseCode") String courseCode, @Param("majorCode") String majorCode,
            @Param("year") Integer year);

    @Query(value = "SELECT lab_credits FROM curriculum_courses cc JOIN curriculums c ON c.curriculum_code = cc.curriculum_code WHERE cc.course_code = :courseCode AND c.major_code = :majorCode AND academic_start_year = :year", nativeQuery = true)
    Integer findCreditsForLabClass(@Param("courseCode") String courseCode, @Param("majorCode") String majorCode,
            @Param("year") Integer year);

}
