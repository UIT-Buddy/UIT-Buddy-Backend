package com.uit.buddy.repository.auth;

import com.uit.buddy.entity.redis.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByMssv(String mssv);

    void deleteByMssv(String mssv);

    boolean existsByMssv(String mssv);
}
