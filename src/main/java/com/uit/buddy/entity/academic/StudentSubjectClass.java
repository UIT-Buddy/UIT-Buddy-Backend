package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractBaseEntity; // Sử dụng UUID làm ID
import com.uit.buddy.enums.StudentClassStatus;
import com.uit.buddy.entity.user.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "student_class", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_class", columnNames = { "mssv", "class_code" })
}, indexes = {
        @Index(name = "idx_st_class_mssv", columnList = "mssv"),
        @Index(name = "idx_st_class_code", columnList = "class_code")
})
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

    @Column(name = "class_code", length = 30, insertable = false, updatable = false)
    private String classCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_code", referencedColumnName = "class_code", foreignKey = @ForeignKey(name = "fk_st_class_subject"))
    private SubjectClass subjectClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private StudentClassStatus status;

    @Column(name = "process_grade")
    private Float processGrade;

    @Column(name = "midterm_grade")
    private Float midtermGrade;

    @Column(name = "final_grade")
    private Float finalGrade;

    @Column(name = "lab_grade")
    private Float labGrade;

    @Column(name = "total_grade")
    private Float totalGrade;
}