package com.uit.buddy.repository.redis;

import com.uit.buddy.entity.redis.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByMssv(String mssv);

    Optional<PasswordResetToken> findByMssvAndIsRevoked(String mssv, boolean isRevoked);
}
