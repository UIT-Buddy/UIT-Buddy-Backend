package com.uit.buddy.entity.learning;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.enums.DeadlineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "temporary_deadline")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporaryDeadline extends AbstractBaseEntity {

    @Column(name = "mssv", nullable = false, length = 12)
    private String mssv;

    @Column(name = "class_code", nullable = false, length = 255)
    private String classCode;

    @Column(name = "deadline_name", nullable = false, length = 255)
    private String deadlineName;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DeadlineStatus status;

    @Column(name = "semester_code", length = 20)
    private String semesterCode;

}
