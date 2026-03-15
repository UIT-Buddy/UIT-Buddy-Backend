package com.uit.buddy.security.impl;

import com.uit.buddy.constant.RedisConstants;
import com.uit.buddy.entity.redis.RefreshToken;
import com.uit.buddy.repository.auth.RefreshTokenRepository;
import com.uit.buddy.security.JwtUtils;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtUtilsImpl implements JwtUtils {

    private static final String CLAIM_TYPE = "token_type";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final long refreshTokenRememberMeExpiration;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> revokeRefreshTokenScript;

    // Constructor Injection
    public JwtUtilsImpl(@Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${app.jwt.refresh-token-remember-me-expiration}") long refreshTokenRememberMeExpiration,
            RefreshTokenRepository refreshTokenRepository, RedisTemplate<String, Object> redisTemplate,
            RedisScript<Long> revokeRefreshTokenScript) {

        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.refreshTokenRememberMeExpiration = refreshTokenRememberMeExpiration;
        this.refreshTokenRepository = refreshTokenRepository;
        this.redisTemplate = redisTemplate;
        this.revokeRefreshTokenScript = revokeRefreshTokenScript;
        log.info("JwtUtils initialized successfully with Lua script support");
    }

    private String createToken(String mssv, long expiration, String type) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder().setSubject(mssv).claim(CLAIM_TYPE, type).setIssuedAt(now).setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS512).compact();
    }

    @Override
    public String generateAccessToken(String mssv) {
        return createToken(mssv, accessTokenExpiration, RedisConstants.TOKEN_TYPE_ACCESS);
    }

    @Override
    public String generateRefreshToken(String mssv, boolean rememberMe) {
        refreshTokenRepository.deleteByMssv(mssv);

        long expiration = rememberMe ? refreshTokenRememberMeExpiration : refreshTokenExpiration;
        String refreshToken = createToken(mssv, expiration, RedisConstants.TOKEN_TYPE_REFRESH);

        RefreshToken token = RefreshToken.builder().refreshToken(refreshToken).mssv(mssv)
                .ttl(TimeUnit.MILLISECONDS.toSeconds(expiration)).build();
        refreshTokenRepository.save(token);

        log.debug("Generated and saved refresh token for user (rememberMe: {})", rememberMe);
        return refreshToken;
    }

    @Override
    public void revokeSpecificRefreshToken(String refreshToken) {
        refreshTokenRepository.deleteById(refreshToken);
        log.info("Specific refresh token revoked");
    }

    @Override
    public void revokeAllSessions(String mssv) {
        try {
            Long deletedCount = redisTemplate.execute(revokeRefreshTokenScript,
                    Collections.singletonList(RedisConstants.REFRESH_TOKEN_KEY_PATTERN), mssv);
            log.info("All sessions revoked for MSSV: {} (deleted {} tokens)", mssv, deletedCount);
        } catch (Exception e) {
            log.error("Failed to revoke sessions using Lua script for MSSV: {}, falling back to repository", mssv, e);
            refreshTokenRepository.deleteByMssv(mssv);
        }
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        return claimsResolver.apply(claims);
    }

    @Override
    public String getMssvFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    @Override
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
