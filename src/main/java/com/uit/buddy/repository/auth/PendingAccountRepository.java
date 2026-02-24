package com.uit.buddy.repository.auth;

import com.uit.buddy.entity.redis.PendingAccount;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingAccountRepository extends CrudRepository<PendingAccount, String> {

    boolean existsBySignupToken(String signupToken);

    Optional<PendingAccount> findBySignupToken(String signupToken);
}
