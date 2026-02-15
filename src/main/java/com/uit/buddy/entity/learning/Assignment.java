package com.uit.buddy.entity.learning;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.academic.SubjectClass;
import com.uit.buddy.enums.AssignmentType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments", indexes = {
        @Index(name = "idx_assignment_class", columnList = "class_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Assignment extends AbstractBaseEntity {

    @Column(name = "class_code", length = 30, insertable = false, updatable = false)
    private String classCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_code", referencedColumnName = "class_code", foreignKey = @ForeignKey(name = "fk_assignment_class"))
    private SubjectClass subjectClass;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private AssignmentType type;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "open_date")
    private LocalDateTime openDate;

    @Column(name = "external_url", length = 512)
    private String externalUrl;
}