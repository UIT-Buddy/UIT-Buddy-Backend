package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.enums.AcademicRank;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "semester_summaries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_semester_summary", columnNames = { "mssv",
                "semester_code" }) }, indexes = { @Index(name = "idx_semester_summary_student", columnList = "mssv"),
                        @Index(name = "idx_semester_summary_semester", columnList = "semester_code") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemesterSummary extends AbstractBaseEntity {

    @Column(name = "mssv", length = 12, insertable = false, updatable = false)
    private String mssv;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mssv", referencedColumnName = "mssv", foreignKey = @ForeignKey(name = "fk_summary_student"))
    private Student student;

    @Column(name = "semester_code", length = 20, insertable = false, updatable = false)
    private String semesterCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_code", referencedColumnName = "semester_code", foreignKey = @ForeignKey(name = "fk_summary_semester"))
    private Semester semester;

    @Column(name = "term_gpa_scale10", precision = 4, scale = 2)
    private BigDecimal termGpaScale10;

    @Column(name = "term_gpa_scale4", precision = 4, scale = 2)
    private BigDecimal termGpaScale4;

    @Column(name = "term_credits")
    private Integer termCredits;

    @Column(name = "accumulated_credits")
    private Integer accumulatedCredits;

    @Column(name = "term_dc_credits")
    private Integer termDcCredits;

    @Column(name = "term_csnn_credits")
    private Integer termCsnnCredits;

    @Column(name = "term_csn_credits")
    private Integer termCsnCredits;

    @Column(name = "term_cn_credits")
    private Integer termCnCredits;

    @Column(name = "term_tottn_credits")
    private Integer termTottnCredits;

    @Column(name = "term_tc_credits")
    private Integer termTcCredits;

    @Column(name = "term_ct_credits")
    private Integer termCtCredits;

    @Enumerated(EnumType.STRING)
    @Column(name = "term_rank", length = 50)
    private AcademicRank termRank;
}
