package com.uit.buddy.repository.auth;

import com.uit.buddy.entity.auth.User;
import com.uit.buddy.enums.auth.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMssv(String mssv);

    Optional<User> findByEmailOrMssv(String email, String mssv);

    boolean existsByEmail(String email);

    boolean existsByMssv(String mssv);

    List<User> findByStatus(UserStatus status);

    // Find locked accounts
    @Query("SELECT u FROM User u WHERE u.isLocked = true AND u.lockedUntil < :now")
    List<User> findExpiredLockedAccounts(@Param("now") LocalDateTime now);

    // Find users with failed login attempts
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :attempts")
    List<User> findUsersWithFailedAttempts(@Param("attempts") int attempts);

    // Reset failed login attempts
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") UUID userId);

    // Lock account
    @Modifying
    @Query("UPDATE User u SET u.isLocked = true, u.lockedUntil = :lockedUntil WHERE u.id = :userId")
    void lockAccount(@Param("userId") UUID userId, @Param("lockedUntil") LocalDateTime lockedUntil);

    // Unlock account
    @Modifying
    @Query("UPDATE User u SET u.isLocked = false, u.lockedUntil = null, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") UUID userId);

    // Activate account
    @Modifying
    @Query("UPDATE User u SET u.isActivated = true, u.status = :status WHERE u.id = :userId")
    void activateAccount(@Param("userId") UUID userId, @Param("status") UserStatus status);

    // Update last login
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :time WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("time") LocalDateTime time);

    // Count by status
    long countByStatus(UserStatus status);

    // Find pending activation users
    @Query("SELECT u FROM User u WHERE u.isActivated = false AND u.status = 'PENDING' AND u.createdAt < :before")
    List<User> findPendingActivationBefore(@Param("before") LocalDateTime before);
}
