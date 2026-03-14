package com.uit.buddy.repository.auth;

import com.uit.buddy.entity.redis.PasswordResetOtp;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetOtp, String> {

    boolean existsById(String mssv);
}
