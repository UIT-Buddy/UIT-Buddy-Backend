package com.uit.buddy.repository.redis;

import com.uit.buddy.entity.redis.TempToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TempTokenRepository extends CrudRepository<TempToken, String> {
    Optional<TempToken> findByMssv(String mssv);

    Optional<TempToken> findByMssvAndIsRevoked(String mssv, boolean isRevoked);
}
