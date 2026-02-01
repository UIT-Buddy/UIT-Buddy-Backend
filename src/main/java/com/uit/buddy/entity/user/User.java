package com.uit.buddy.entity.user;

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

    @Column(name = "email")
    private String email;

    @Column(name = "mssv", nullable = false, unique = true, length = 10)
    private String mssv;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", length = 50)
    private String fullName;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
