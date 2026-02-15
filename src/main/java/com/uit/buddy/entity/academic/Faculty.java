package com.uit.buddy.entity.academic;

import com.uit.buddy.entity.AbstractAuditEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faculties", indexes = {
        @Index(name = "idx_faculty_code", columnList = "faculty_code", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Faculty extends AbstractAuditEntity {

    @Id
    @Column(name = "faculty_code", length = 20, nullable = false, unique = true)
    private String facultyCode;

    @Column(name = "faculty_name", nullable = false, length = 150)
    private String facultyName;

    @Column(name = "office_location", length = 150)
    private String officeLocation;
}
