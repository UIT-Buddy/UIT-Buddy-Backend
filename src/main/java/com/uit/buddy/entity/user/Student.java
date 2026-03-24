package com.uit.buddy.entity.user;

import com.uit.buddy.entity.AbstractAuditEntity;
import com.uit.buddy.entity.academic.HomeClass;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students", indexes = { @Index(name = "idx_student_mssv", columnList = "mssv", unique = true),
        @Index(name = "idx_student_home_class", columnList = "home_class_code") })
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
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "bio", length = 100)
    private String bio;

    @Column(name = "comet_uid", nullable = false, length = 100)
    private String cometUid;

    @Column(name = "home_class_code", length = 20, nullable = false)
    private String homeClassCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_class_code", referencedColumnName = "home_class_code", foreignKey = @ForeignKey(name = "fk_student_home_class"), insertable = false, updatable = false, nullable = false)
    private HomeClass homeClass;

    @Column(name = "encrypted_wstoken", length = 512)
    private String encryptedWstoken;

    @Column(name = "comet_auth_token", length = 512)
    private String cometAuthToken;

    @OneToOne(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserSetting userSetting;
}
