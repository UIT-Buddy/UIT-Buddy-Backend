package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "home_classes", indexes = {
        @Index(name = "idx_home_class_code", columnList = "home_class_code", unique = true),
        @Index(name = "idx_home_class_major", columnList = "major_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeClass extends AbstractAuditEntity {

    @Id
    @Column(name = "home_class_code", length = 20, nullable = false, unique = true)
    private String homeClassCode;

    @Column(name = "academic_year", nullable = false, length = 10)
    private String academicYear;

    @Column(name = "advisor_name", length = 150)
    private String advisorName;

    @Column(name = "major_code", length = 20, nullable = false, insertable = false, updatable = false)
    private String majorCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "major_code", referencedColumnName = "major_code", foreignKey = @ForeignKey(name = "fk_home_class_major"))
    private Major major;
}
