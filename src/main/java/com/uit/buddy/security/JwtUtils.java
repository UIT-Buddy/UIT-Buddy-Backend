package com.uit.buddy.security;

public interface JwtUtils {

    String generateAccessToken(String mssv);

    String generateRefreshToken(String mssv);

    String getMssvFromToken(String token);

    boolean validateToken(String token);

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();

    void revokeSpecificRefreshToken(String refreshToken);

    void revokeAllSessions(String mssv);
}
