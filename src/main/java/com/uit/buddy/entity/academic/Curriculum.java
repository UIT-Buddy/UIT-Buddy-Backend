package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity; // Chuyển sang AuditEntity để tự quản lý ID
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "curriculums",
    indexes = {
      @Index(name = "idx_curriculum_major", columnList = "major_code"),
      @Index(name = "idx_curriculum_start_year", columnList = "academic_start_year")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curriculum extends AbstractAuditEntity {

  @Id
  @Column(name = "curriculum_code", length = 50)
  private String curriculumCode;

  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Column(name = "major_code", length = 20, insertable = false, updatable = false)
  private String majorCode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "major_code",
      referencedColumnName = "major_code",
      foreignKey = @ForeignKey(name = "fk_curriculum_major"))
  private Major major;

  @Column(name = "academic_start_year")
  private Integer academicStartYear;

  @Column(name = "total_credits_required")
  private Integer totalCreditsRequired;

  @Column(name = "min_general_education_credits")
  private Integer minGeneralEducationCredits;

  @Column(name = "min_professional_education_credits")
  private Integer minProfessionalEducationCredits;

  @Column(name = "min_graduation_credits")
  private Integer minGraduationCredits;
}
