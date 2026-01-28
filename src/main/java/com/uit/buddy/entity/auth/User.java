package com.uit.buddy.entity.auth;

import com.uit.buddy.entity.AbstractBaseEntity;
import com.uit.buddy.enums.auth.UserRole;
import com.uit.buddy.enums.auth.UserStatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_mssv", columnList = "mssv")
})
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

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "is_activated", nullable = false)
    @Builder.Default
    private Boolean isActivated = false;

    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lockAccount(int lockDurationMinutes) {
        this.isLocked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }

    public void unlockAccount() {
        this.isLocked = false;
        this.lockedUntil = null;
        this.failedLoginAttempts = 0;
    }

    public boolean isAccountLocked() {
        if (!this.isLocked) {
            return false;
        }
        if (this.lockedUntil != null && LocalDateTime.now().isAfter(this.lockedUntil)) {
            unlockAccount();
            return false;
        }
        return true;
    }

    public void activate() {
        this.isActivated = true;
        this.status = UserStatus.ACTIVE;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }
}
