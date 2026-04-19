package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grades", uniqueConstraints = {
        @UniqueConstraint(name = "uk_grade_student_course_semester", columnNames = { "mssv", "course_code",
                "semester_code" }) }, indexes = { @Index(name = "idx_grade_mssv", columnList = "mssv"),
                        @Index(name = "idx_grade_semester", columnList = "semester_code"),
                        @Index(name = "idx_grade_mssv_semester", columnList = "mssv, semester_code"),
                        @Index(name = "idx_grade_course", columnList = "course_code") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Grade extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, nullable = false)
    private String mssv;

    @Column(name = "semester_code", length = 20, nullable = false)
    private String semesterCode;

    @Column(name = "course_code", length = 20, nullable = false)
    private String courseCode;

    @Column(name = "course_name")
    private String courseName;

    @Column(name = "credits")
    private Integer credits;

    @Column(name = "course_type", length = 20)
    private String courseType;

    @Column(name = "process_grade")
    private Float processGrade;

    @Column(name = "midterm_grade")
    private Float midtermGrade;

    @Column(name = "final_grade")
    private Float finalGrade;

    @Column(name = "lab_grade")
    private Float labGrade;

    @Column(name = "total_grade")
    private Float totalGrade;
}
