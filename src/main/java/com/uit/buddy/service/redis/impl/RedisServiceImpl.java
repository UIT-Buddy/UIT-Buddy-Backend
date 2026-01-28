package com.uit.buddy.service.redis.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.uit.buddy.service.redis.RedisService;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String OTP_PREFIX = "otp:";
    private static final String OTP_ATTEMPTS_PREFIX = "otp_attempts:";
    private static final String TEMP_TOKEN_PREFIX = "temp_token:";
    private static final String PENDING_SIGNUP_PREFIX = "pending_signup:";

    public void saveRefreshToken(String userId, String refreshToken, long expirationMs) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationMs, TimeUnit.MILLISECONDS);
        log.debug("Saved refresh token for user: {}", userId);
    }

    public String getRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void deleteRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Deleted refresh token for user: {}", userId);
    }

    public void saveOtp(String email, String otp, long expirationMinutes) {
        String key = OTP_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(expirationMinutes));
        log.debug("Saved OTP for email: {}", email);
    }

    public String getOtp(String email) {
        String key = OTP_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void deleteOtp(String email) {
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Deleted OTP for email: {}", email);
    }

    public long incrementOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email;
        Long attempts = redisTemplate.opsForValue().increment(key);

        // Set expiration for attempts counter (same as OTP expiration)
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(5));
        }

        return attempts != null ? attempts : 0;
    }

    public long getOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Long.parseLong(value.toString()) : 0;
    }

    public void resetOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email;
        redisTemplate.delete(key);
        log.debug("Reset OTP attempts for email: {}", email);
    }

    public boolean hasKey(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    public void setValue(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object getValue(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    public void saveTempToken(String token, String mssv, long expirationMinutes) {
        String key = TEMP_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, mssv, Duration.ofMinutes(expirationMinutes));
        log.debug("Saved temp token for mssv: {}", mssv);
    }

    public String getMssvFromTempToken(String token) {
        String key = TEMP_TOKEN_PREFIX + token;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void deleteTempToken(String token) {
        String key = TEMP_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
        log.debug("Deleted temp token");
    }

    public void savePendingSignup(String mssv, long expirationMinutes) {
        String key = PENDING_SIGNUP_PREFIX + mssv;
        redisTemplate.opsForValue().set(key, "pending", Duration.ofMinutes(expirationMinutes));
        log.debug("Saved pending signup for mssv: {}", mssv);
    }

    public boolean hasPendingSignup(String mssv) {
        String key = PENDING_SIGNUP_PREFIX + mssv;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void deletePendingSignup(String mssv) {
        String key = PENDING_SIGNUP_PREFIX + mssv;
        redisTemplate.delete(key);
        log.debug("Deleted pending signup for mssv: {}", mssv);
    }
}
