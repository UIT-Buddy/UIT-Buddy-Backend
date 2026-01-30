package com.uit.buddy.security.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.uit.buddy.security.JwtUserDetails;
import com.uit.buddy.security.JwtUtils;
import com.uit.buddy.entity.redis.RefreshToken;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.repository.redis.RefreshTokenRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtilsImpl implements JwtUtils {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisScript<Long> revokeTokenScript;

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.token.expires-in}")
    private long jwtExpiration;

    @Value("${app.jwt.refresh-token.expires-in}")
    private long jwtRefreshTokenExpiresIn;

    @Value("${app.jwt.refresh-token.expires-in-with-rememberme}")
    private long jwtRefreshTokenExpiresInWithRememberme;

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractMssv(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    @Override
    public String generateToken(JwtUserDetails userDetails) {
        return generateAccessTokenByJwtUserDetails(new HashMap<>(), userDetails);
    }

    @Override
    public String generateAccessTokenByJwtUserDetails(Map<String, Object> extraClaims, JwtUserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    @Override
    public String generateRefreshTokenByJwtUserDetails(Map<String, Object> extraClaims, JwtUserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtRefreshTokenExpiresIn);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            JwtUserDetails userDetails,
            long expiration) {
        extraClaims.put("email", userDetails.getEmail());

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getMssv())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String accessToken, JwtUserDetails userDetails) {
        final String mssv = extractMssv(accessToken);
        return (mssv.equals(userDetails.getMssv())) && !isTokenExpired(accessToken);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generateAccessTokenByRefreshToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (token.isRevoked()) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String redisKey = "refresh_tokens:" + refreshToken;

        Long result = redisTemplate.execute(
                revokeTokenScript,
                Collections.singletonList(redisKey),
                "isRevoked",
                "0");

        if (result == null || result == -1) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        if (result == 0) {
            revokeAllRefreshTokensByFamilyTokenInRefreshToken(refreshToken);
            throw new AuthException(AuthErrorCode.SUSPICIOUS_DETECTED);
        }

        // Generate new access token
        Map<String, Object> claims = new HashMap<>();

        String mssv = extractMssv(refreshToken);

        claims.put("email", extractEmail(refreshToken));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(mssv)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private void revokeAllRefreshTokensByFamilyTokenInRefreshToken(String refreshToken) {
        Claims claims = extractAllClaims(refreshToken);
        String familyToken = claims.get("family_token", String.class);
        revokeRefreshTokenByFamilyToken(familyToken);
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<Void> revokeRefreshTokenByFamilyToken(String familyToken) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByFamilyTokenAndIsRevoked(familyToken, false);

        for (RefreshToken rt : refreshTokens) {
            rt.setRevoked(true);
        }

        refreshTokenRepository.saveAll(refreshTokens);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void revokeRefreshTokenByRefreshToken(String refreshToken) {
        RefreshToken storedRefreshToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND));
        if (storedRefreshToken.isRevoked()) {
            throw new AuthException(AuthErrorCode.SUSPICIOUS_DETECTED);
        }
        storedRefreshToken.setRevoked(true);
        refreshTokenRepository.save(storedRefreshToken);

    }

    @Override
    public void revokeAllRefreshTokensByMssv(String mssv) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByMssvAndIsRevoked(mssv, false);

        for (RefreshToken rt : refreshTokens) {
            rt.setRevoked(true);
        }

        refreshTokenRepository.saveAll(refreshTokens);
    }

    @Override
    public Map<String, String> generateRefreshTokenByJwtUserDetails(JwtUserDetails userDetails, String familyToken,
            Boolean rememberMe) {

        if (familyToken != null) {
            List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByFamilyToken(familyToken);
            for (RefreshToken rt : refreshTokens) {
                rt.setRevoked(true);
            }
            refreshTokenRepository.saveAll(refreshTokens);
        }

        long refreshTokenExpiresIn = rememberMe ? jwtRefreshTokenExpiresInWithRememberme : jwtRefreshTokenExpiresIn;

        String newFamilyToken = (familyToken != null) ? familyToken : UUID.randomUUID().toString();
        String mssv = userDetails.getMssv();
        String email = userDetails.getEmail();

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("family_token", newFamilyToken);
        extraClaims.put("email", email);

        String refreshToken = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(mssv)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiresIn))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .refreshToken(refreshToken)
                .familyToken(newFamilyToken)
                .mssv(mssv)
                .email(email)
                .ttl(refreshTokenExpiresIn / 1000)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return Map.of("refresh_token", refreshToken, "family_token", newFamilyToken);
    }
}
