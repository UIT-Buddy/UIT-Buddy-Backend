package com.uit.buddy.service.auth.impl;

import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.service.auth.OtpService;
import com.uit.buddy.service.email.EmailService;
import com.uit.buddy.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final RedisService redisService;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRATION_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long RESEND_COOLDOWN_SECONDS = 120; // 2 minutes
    private static final long TEMP_TOKEN_EXPIRATION_MINUTES = 10; // 10 minutes for password setting
    private static final String OTP_RESEND_PREFIX = "otp_resend:";

    @Override
    public void sendForgetPasswordOtp(String email) {

        if (isResendCooldownActive(email)) {
            throw new AuthException(AuthErrorCode.TOO_MANY_OTP_REQUESTS);
        }

        String otp = generateOtp();
        redisService.saveOtp(email, otp, OTP_EXPIRATION_MINUTES);

        setResendCooldown(email);

        redisService.resetOtpAttempts(email);

        // Send OTP via email
        emailService.sendOtpEmail(email, otp);
        log.info("OTP sent to: {}", email);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        String storedOtp = redisService.getOtp(email);

        if (storedOtp == null) {
            throw new AuthException(AuthErrorCode.OTP_NOT_FOUND);
        }

        long attempts = redisService.getOtpAttempts(email);
        if (attempts >= MAX_OTP_ATTEMPTS) {
            redisService.deleteOtp(email);
            redisService.resetOtpAttempts(email);
            throw new AuthException(AuthErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (!storedOtp.equals(otp)) {
            redisService.incrementOtpAttempts(email);
            throw new AuthException(AuthErrorCode.OTP_INVALID);
        }

        redisService.deleteOtp(email);
        redisService.resetOtpAttempts(email);
        log.info("OTP verified successfully for: {}", email);
    }

    @Override
    public long getRemainingCooldown(String email) {
        String key = OTP_RESEND_PREFIX + email;
        Object value = redisService.getValue(key);
        return value != null ? RESEND_COOLDOWN_SECONDS : 0;
    }

    @Override
    public void sendSignupOtp(String mssv) {
        String email = mssv + "@gm.uit.edu.vn";

        if (isResendCooldownActive(email)) {
            throw new AuthException(AuthErrorCode.TOO_MANY_OTP_REQUESTS);
        }

        String otp = generateOtp();

        redisService.saveOtp(email, otp, OTP_EXPIRATION_MINUTES);

        redisService.savePendingSignup(mssv, OTP_EXPIRATION_MINUTES);

        setResendCooldown(email);

        redisService.resetOtpAttempts(email);

        emailService.sendOtpEmail(email, otp);
        log.info("Signup OTP sent to: {}", email);
    }

    @Override
    public String verifySignupOtp(String mssv, String otp) {
        String email = mssv + "@gm.uit.edu.vn";

        String storedOtp = redisService.getOtp(email);

        if (storedOtp == null) {
            throw new AuthException(AuthErrorCode.OTP_NOT_FOUND);
        }

        long attempts = redisService.getOtpAttempts(email);
        if (attempts >= MAX_OTP_ATTEMPTS) {
            redisService.deleteOtp(email);
            redisService.resetOtpAttempts(email);
            redisService.deletePendingSignup(mssv);
            throw new AuthException(AuthErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (!storedOtp.equals(otp)) {
            redisService.incrementOtpAttempts(email);
            throw new AuthException(AuthErrorCode.OTP_INVALID);
        }

        redisService.deleteOtp(email);
        redisService.resetOtpAttempts(email);

        String tempToken = generateTempToken();
        redisService.saveTempToken(tempToken, mssv, TEMP_TOKEN_EXPIRATION_MINUTES);

        log.info("Signup OTP verified for mssv: {}, temp token generated", mssv);
        return tempToken;
    }

    @Override
    public String validateTempToken(String tempToken) {
        String mssv = redisService.getMssvFromTempToken(tempToken);
        if (mssv == null) {
            throw new AuthException(AuthErrorCode.TEMP_TOKEN_INVALID);
        }
        return mssv;
    }

    @Override
    public void consumeTempToken(String tempToken) {
        String mssv = redisService.getMssvFromTempToken(tempToken);
        if (mssv != null) {
            redisService.deleteTempToken(tempToken);
            redisService.deletePendingSignup(mssv);
        }
    }

    private boolean isResendCooldownActive(String email) {
        String key = OTP_RESEND_PREFIX + email;
        return redisService.hasKey(key);
    }

    private void setResendCooldown(String email) {
        String key = OTP_RESEND_PREFIX + email;
        redisService.setValue(key, "1", RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
    }

    private String generateTempToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        StringBuilder token = new StringBuilder();
        for (byte b : bytes) {
            token.append(String.format("%02x", b));
        }
        return token.toString();
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }
}
