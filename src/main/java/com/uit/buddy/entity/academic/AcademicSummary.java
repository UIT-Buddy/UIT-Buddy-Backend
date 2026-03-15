package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "academic_summary", indexes = {
        @Index(name = "idx_academic_summary_mssv", columnList = "mssv", unique = true) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicSummary extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, nullable = false, insertable = false, updatable = false)
    private String mssv;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_summary_student"))
    private Student student;

    @Column(name = "attempted_credits")
    private Integer attemptedCredits;

    @Column(name = "accumulated_credits")
    private Integer accumulatedCredits;

    @Column(name = "attempted_gpa")
    private Float attemptedGpa;

    @Column(name = "accumulated_gpa")
    private Float accumulatedGpa;

    @Column(name = "major_progress")
    private Float majorProgress;

    @Column(name = "accumulated_general_credits")
    private Integer accumulatedGeneralCredits;

    @Column(name = "accumulated_foundation_credits")
    private Integer accumulatedFoundationCredits;

    @Column(name = "accumulated_major_credits")
    private Integer accumulatedMajorCredits;

    @Column(name = "accumulated_elective_credits")
    private Integer accumulatedElectiveCredits;

    @Column(name = "accumulated_graduation_credits")
    private Integer accumulatedGraduationCredits;
}
