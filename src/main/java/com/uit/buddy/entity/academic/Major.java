package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "majors", indexes = {
        @Index(name = "idx_major_code", columnList = "major_code", unique = true),
        @Index(name = "idx_major_faculty", columnList = "faculty_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Major extends AbstractAuditEntity {

    @Id
    @Column(name = "major_code", length = 20, nullable = false, unique = true)
    private String majorCode;

    @Column(name = "major_name", nullable = false, length = 150)
    private String majorName;

    @Column(name = "major_number_code", length = 50, nullable = false, unique = true)
    private String majorNumberCode;

    @Column(name = "faculty_code", length = 20, nullable = false, insertable = false, updatable = false)
    private String facultyCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_code", referencedColumnName = "faculty_code", foreignKey = @ForeignKey(name = "fk_major_faculty"))
    private Faculty faculty;
}
