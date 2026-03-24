package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractBaseEntity; // Sử dụng UUID làm ID
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.StudentClassStatus;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "student_class", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_class", columnNames = { "mssv", "class_id" }) }, indexes = {
                @Index(name = "idx_st_class_mssv", columnList = "mssv"),
                @Index(name = "idx_st_class_id", columnList = "class_id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSubjectClass extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_st_class_student"))
    private Student student;

    @Column(name = "class_id", length = 30, insertable = false, updatable = false)
    private UUID class_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_st_class_subject"))
    private SubjectClass subjectClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StudentClassStatus status;

}
