package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "course_categories",
    indexes = {@Index(name = "idx_category_code", columnList = "category_code", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCategory extends AbstractAuditEntity {

  @Id
  @Column(name = "category_code", length = 20)
  private String categoryCode;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;
}
