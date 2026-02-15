package com.uit.buddy.entity.user;

import com.uit.buddy.entity.AbstractAuditEntity;
import com.uit.buddy.entity.academic.HomeClass;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students", indexes = {
        @Index(name = "idx_student_mssv", columnList = "mssv", unique = true),
        @Index(name = "idx_student_home_class", columnList = "home_class_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student extends AbstractAuditEntity {

    @Id
    @Column(name = "mssv", unique = true, nullable = false, length = 12)
    private String mssv;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "email", nullable = false, length = 150)
    @Setter(AccessLevel.NONE)
    private String email;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "comet_uid", nullable = false, length = 100)
    private String cometUid;

    @Column(name = "home_class_code", length = 20, nullable = false, insertable = false, updatable = false)
    private String homeClassCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_class_code", referencedColumnName = "home_class_code", foreignKey = @ForeignKey(name = "fk_student_home_class"))
    private HomeClass homeClass;

    @Column(name = "encrypted_wstoken", length = 512)
    private String encryptedWstoken;

    @PrePersist
    private void generateEmail() {
        if (this.mssv != null && this.email == null) {
            this.email = this.mssv + "@gm.uit.edu.vn";
        }
    }
}
