package com.uit.buddy.repository.auth;

import com.uit.buddy.entity.redis.RefreshToken;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByMssv(String mssv);

    void deleteByMssv(String mssv);

    boolean existsByMssv(String mssv);
}
