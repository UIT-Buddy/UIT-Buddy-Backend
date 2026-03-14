package com.uit.buddy.repository.auth;

import com.uit.buddy.entity.redis.PendingAccount;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PendingAccountRepository extends CrudRepository<PendingAccount, String> {

  boolean existsBySignupToken(String signupToken);

  Optional<PendingAccount> findBySignupToken(String signupToken);
}
