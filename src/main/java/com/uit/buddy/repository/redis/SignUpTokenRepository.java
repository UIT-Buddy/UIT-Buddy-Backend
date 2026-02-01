package com.uit.buddy.repository.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.auth.SignUpToken;

import java.util.Optional;

@Repository
public interface SignUpTokenRepository extends CrudRepository<SignUpToken, String> {
    Optional<SignUpToken> findByMssv(String mssv);

    Optional<SignUpToken> findByMssvAndIsRevoked(String mssv, boolean isRevoked);
}