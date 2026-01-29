package com.uit.buddy.repository.auth;

import com.uit.buddy.entity.auth.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMssv(String mssv);

    Optional<User> findByEmailOrMssv(String email, String mssv);

    boolean existsByEmail(String email);

    boolean existsByMssv(String mssv);

    // Verify account
    @Modifying
    @Query("UPDATE User u SET u.isVerified = true WHERE u.id = :userId")
    void verifyAccount(@Param("userId") UUID userId);
}
