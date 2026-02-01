package com.uit.buddy.repository.redis;

import org.springframework.stereotype.Repository;

import com.uit.buddy.entity.auth.RefreshToken;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    RefreshToken findByMssv(String mssv);

    List<RefreshToken> findAllByMssv(String mssv);

    List<RefreshToken> findAllByMssvAndIsRevoked(String mssv, boolean isRevoked);

}
