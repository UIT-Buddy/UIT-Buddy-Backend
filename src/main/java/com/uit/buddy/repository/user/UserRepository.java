package com.uit.buddy.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.user.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMssv(String mssv);

    Optional<User> findByEmailOrMssv(String email, String mssv);

    boolean existsByEmail(String email);

    boolean existsByMssv(String mssv);
}
