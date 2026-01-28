package com.uit.buddy.service.redis;

import java.util.concurrent.TimeUnit;

public interface RedisService {

    void setValue(String key, Object value, long timeout, TimeUnit unit);

    Object getValue(String key);

    void deleteKey(String key);

    boolean hasKey(String key);

    void saveRefreshToken(String userId, String refreshToken, long expirationMs);

    String getRefreshToken(String userId);

    void deleteRefreshToken(String userId);

    void saveOtp(String email, String otp, long expirationMinutes);

    String getOtp(String email);

    void deleteOtp(String email);

    long incrementOtpAttempts(String email);

    long getOtpAttempts(String email);

    void resetOtpAttempts(String email);

    void saveTempToken(String token, String mssv, long expirationMinutes);

    String getMssvFromTempToken(String token);

    void deleteTempToken(String token);

    void savePendingSignup(String mssv, long expirationMinutes);

    boolean hasPendingSignup(String mssv);

    void deletePendingSignup(String mssv);
}
