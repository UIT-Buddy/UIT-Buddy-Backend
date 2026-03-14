package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "semester_summaries",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_student_semester_summary",
          columnNames = {"mssv", "semester_code"})
    },
    indexes = {
      @Index(name = "idx_semester_summary_student", columnList = "mssv"),
      @Index(name = "idx_semester_summary_semester", columnList = "semester_code")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterSummary extends AbstractBaseEntity {

  @Column(name = "mssv", length = 12, insertable = false, updatable = false)
  private String mssv;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "mssv",
      referencedColumnName = "mssv",
      foreignKey = @ForeignKey(name = "fk_summary_student"))
  private Student student;

  @Column(name = "semester_code", length = 20, insertable = false, updatable = false)
  private String semesterCode;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "semester_code",
      referencedColumnName = "semester_code",
      foreignKey = @ForeignKey(name = "fk_summary_semester"))
  private Semester semester;

  @Column(name = "term_gpa")
  private Float termGpa;

  @Column(name = "term_credits")
  private Integer termCredits;

  @Column(name = "term_rank", length = 50)
  private String termRank;
}
