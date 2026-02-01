package com.uit.buddy.security;

import io.jsonwebtoken.Claims;
import java.util.Map;
import java.util.function.Function;

public interface JwtUtils {

    String extractMssv(String token);

    String extractEmail(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    String generateToken(JwtUserDetails userDetails);

    String generateAccessTokenByJwtUserDetails(Map<String, Object> extraClaims, JwtUserDetails userDetails);

    String generateRefreshTokenByJwtUserDetails(Map<String, Object> extraClaims, JwtUserDetails userDetails);

    Map<String, String> generateRefreshTokenByJwtUserDetails(JwtUserDetails userDetails, Boolean rememberMe);

    boolean validateAccessToken(String accessToken, JwtUserDetails userDetails);

    String generateAccessTokenByRefreshToken(String refreshToken);

    void revokeRefreshTokenByRefreshToken(String refreshToken);

    void revokeAllRefreshTokensByMssv(String mssv);
}
