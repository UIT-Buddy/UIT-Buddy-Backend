package com.uit.buddy.entity.auth;

import com.uit.buddy.entity.AbstractBaseEntity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AbstractBaseEntity {

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "mssv", nullable = false, unique = true, length = 10)
    private String mssv;

    @Column(name = "password", nullable = false, length = 50)
    private String password;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public void verify() {
        this.isVerified = true;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
