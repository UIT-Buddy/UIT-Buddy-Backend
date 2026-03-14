package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity;
import com.uit.buddy.enums.SemesterType;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
    name = "semesters",
    indexes = {@Index(name = "idx_semester_code", columnList = "semester_code", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester extends AbstractAuditEntity {

  @Id
  @Column(name = "semester_code", length = 20, unique = true, nullable = false)
  private String semesterCode;

  @Column(name = "year_start", length = 10, nullable = false)
  private String yearStart;

  @Column(name = "year_end", length = 10)
  private String yearEnd;

  @Enumerated(EnumType.STRING)
  @Column(name = "semester_number", length = 20, nullable = false)
  private SemesterType semesterNumber;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;
}
