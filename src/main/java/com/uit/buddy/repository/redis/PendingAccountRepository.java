package com.uit.buddy.repository.redis;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.auth.PendingAccount;

import java.util.Optional;

@Repository
public interface PendingAccountRepository extends CrudRepository<PendingAccount, String> {
    Optional<PendingAccount> findByMssv(String mssv);
}
