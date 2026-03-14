package com.uit.buddy.entity.learning;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.TaskPriority;
import com.uit.buddy.enums.TaskType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(
    name = "student_tasks",
    indexes = {
      @Index(name = "idx_task_student", columnList = "mssv"),
      @Index(name = "idx_task_assignment", columnList = "assignment_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentTask extends AbstractBaseEntity {

  @Column(name = "mssv", length = 12, insertable = false, updatable = false)
  private String mssv;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(
      name = "mssv",
      referencedColumnName = "mssv",
      foreignKey = @ForeignKey(name = "fk_task_student"))
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignment_id", foreignKey = @ForeignKey(name = "fk_task_assignment"))
  private Assignment assignment;

  @Enumerated(EnumType.STRING)
  @Column(name = "task_type", length = 20)
  private TaskType taskType;

  @Column(name = "personal_title", nullable = false, length = 255)
  private String personalTitle;

  @Column(name = "is_completed", nullable = false)
  @Builder.Default
  private Boolean isCompleted = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "priority", length = 20)
  private TaskPriority priority;

  @Column(name = "reminder_at")
  private LocalDateTime reminderAt;

  @Column(name = "grade")
  private Float grade;
}
