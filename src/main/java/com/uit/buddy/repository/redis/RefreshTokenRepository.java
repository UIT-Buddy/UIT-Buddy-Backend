package com.uit.buddy.repository.redis;

import com.uit.buddy.entity.redis.RefreshToken;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    RefreshToken findByMssv(String mssv);

    List<RefreshToken> findAllByMssv(String mssv);

    List<RefreshToken> findAllByMssvAndIsRevoked(String mssv, boolean isRevoked);

    List<RefreshToken> findAllByFamilyToken(String familyToken);

    List<RefreshToken> findByRefreshTokenAndFamilyToken(String refreshToken, String familyToken);

    List<RefreshToken> findAllByRefreshTokenAndFamilyTokenAndIsRevoked(String refreshToken, String familyToken,
            boolean isRevoked);

    List<RefreshToken> findAllByFamilyTokenAndMssvAndIsRevoked(String familyToken, String Mssv, boolean isRevoked);

    List<RefreshToken> findAllByFamilyTokenAndIsRevoked(String familyToken, boolean isRevoked);

}
