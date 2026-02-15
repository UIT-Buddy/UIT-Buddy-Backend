package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_course_code", columnList = "course_code", unique = true),
        @Index(name = "idx_course_faculty", columnList = "faculty_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course extends AbstractAuditEntity {

    @Id
    @Column(name = "course_code", length = 20, unique = true, nullable = false)
    private String courseCode;

    @Column(name = "course_name", nullable = false, length = 150)
    private String courseName;

    @Column(name = "theory_credits")
    private Integer theoryCredits;

    @Column(name = "lab_credits")
    private Integer labCredits;

    // Score weight
    @Column(name = "process_weight")
    private Integer processWeight;

    @Column(name = "midterm_weight")
    private Integer midtermWeight;

    @Column(name = "final_weight")
    private Integer finalWeight;

    @Column(name = "lab_weight")
    private Integer labWeight;

    @Column(name = "faculty_code", length = 20, insertable = false, updatable = false)
    private String facultyCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faculty_code", referencedColumnName = "faculty_code", foreignKey = @ForeignKey(name = "fk_course_faculty"))
    private Faculty faculty;
}