package com.uit.buddy.service.auth.impl;

import com.uit.buddy.entity.redis.PasswordResetToken;
import com.uit.buddy.entity.redis.PendingAccount;
import com.uit.buddy.entity.redis.SignUpToken;
import com.uit.buddy.entity.redis.TempToken;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.repository.redis.PasswordResetTokenRepository;
import com.uit.buddy.repository.redis.PendingAccountRepository;
import com.uit.buddy.repository.redis.TempTokenRepository;
import com.uit.buddy.repository.redis.SignUpTokenRepository;
import com.uit.buddy.service.auth.OtpService;
import com.uit.buddy.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final PendingAccountRepository pendingAccountRepository;
    private final SignUpTokenRepository signUpTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TempTokenRepository tempTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final RedisScript<Long> verifyOtpScript;
    private final RedisScript<String> validateTempTokenScript;
    private final RedisScript<Long> revokeOldOtpScript;

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiration-seconds}")
    private long otpExpirationSeconds;

    @Value("${app.otp.max-attempts}")
    private int maxOtpAttempts;

    @Value("${app.otp.resend-cooldown-seconds}")
    private long resendCooldownSeconds;

    @Value("${app.temp-token.expiration-seconds}")
    private long tempTokenExpirationSeconds;

    @Value("${app.pending-account.expiration-seconds}")
    private Long pendingAccountExpirationSeconds;

    private static final String OTP_RESEND_PREFIX = "otp_resend:";

    @Override
    public void sendForgetPasswordOtp(String email) {
        if (isResendCooldownActive(email)) {
            throw new AuthException(AuthErrorCode.TOO_MANY_OTP_REQUESTS);
        }

        String mssv = email.replace("@gm.uit.edu.vn", "");

        // Revoke old OTPs before creating new one
        revokeOldOtps(mssv, "password_reset_otp");

        String otp = generateOtp();

        PasswordResetToken token = PasswordResetToken.builder()
                .otp(otp)
                .mssv(mssv)
                .attempts(0)
                .isRevoked(false)
                .ttl(otpExpirationSeconds)
                .build();

        passwordResetTokenRepository.save(token);
        setResendCooldown(email);

        // Send OTP via email
        emailService.sendOtpEmail(email, otp);
        log.info("Password reset OTP sent to: {}", email);
    }

    @Override
    public void verifyOtp(String email, String otp) {
        String mssv = email.replace("@gm.uit.edu.vn", "");

        Long result;
        try {
            result = redisTemplate.execute(
                    verifyOtpScript,
                    Collections.singletonList("password_reset_otp:*"),
                    mssv,
                    otp,
                    String.valueOf(maxOtpAttempts));
        } catch (Exception e) {
            log.error("Redis error during OTP verification for email: {}", email, e);
            throw new AuthException(AuthErrorCode.OTP_NOT_FOUND);
        }

        if (result == null || result == -1) {
            throw new AuthException(AuthErrorCode.OTP_NOT_FOUND);
        }

        if (result == -2) {
            throw new AuthException(AuthErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (result == 0) {
            throw new AuthException(AuthErrorCode.OTP_INVALID);
        }

        log.info("Password reset OTP verified successfully for: {}", email);
    }

    @Override
    public long getRemainingCooldown(String email) {
        String key = OTP_RESEND_PREFIX + email;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return (ttl != null && ttl > 0) ? ttl : 0;
    }

    @Override
    public void sendSignupOtp(String mssv) {
        String email = mssv + "@gm.uit.edu.vn";

        if (isResendCooldownActive(email)) {
            throw new AuthException(AuthErrorCode.TOO_MANY_OTP_REQUESTS);
        }

        revokeOldOtps(mssv, "signup_otp");

        PendingAccount pendingAccount = PendingAccount.builder()
                .mssv(mssv)
                .email(email)
                .isRevoked(false)
                .ttl(pendingAccountExpirationSeconds)
                .build();

        pendingAccountRepository.save(pendingAccount);

        log.info("Pending account created for mssv: {}", mssv);

        String otp = generateOtp();

        SignUpToken token = SignUpToken.builder()
                .otp(otp)
                .mssv(mssv)
                .attempts(0)
                .isRevoked(false)
                .ttl(otpExpirationSeconds)
                .build();

        signUpTokenRepository.save(token);
        setResendCooldown(email);

        emailService.sendOtpEmail(email, otp);
        log.info("Signup OTP sent to: {}", email);
    }

    @Override
    public String verifySignupOtp(String mssv, String otp) {
        Long result;
        try {
            result = redisTemplate.execute(
                    verifyOtpScript,
                    Collections.singletonList("signup_otp:*"),
                    mssv,
                    otp,
                    String.valueOf(maxOtpAttempts));
        } catch (Exception e) {
            log.error("Redis error during OTP verification for mssv: {}", mssv, e);
            throw new AuthException(AuthErrorCode.OTP_NOT_FOUND);
        }

        if (result == null || result == -1) {
            throw new AuthException(AuthErrorCode.OTP_NOT_FOUND);
        }

        if (result == -2) {
            throw new AuthException(AuthErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (result == 0) {
            throw new AuthException(AuthErrorCode.OTP_INVALID);
        }

        PendingAccount pendingAccount = pendingAccountRepository.findByMssv(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.PENDING_ACCOUNT_NOT_FOUND));

        if (pendingAccount.isRevoked()) {
            throw new AuthException(AuthErrorCode.PENDING_ACCOUNT_EXPIRED);
        }

        // Generate temp token
        String tempTokenValue = generateTempToken();
        TempToken tempToken = TempToken.builder()
                .token(tempTokenValue)
                .mssv(mssv)
                .isRevoked(false)
                .ttl(tempTokenExpirationSeconds)
                .build();

        tempTokenRepository.save(tempToken);

        log.info("Signup OTP verified for mssv: {}, temp token generated", mssv);
        return tempTokenValue;
    }

    @Override
    public String validateTempToken(String token) {
        String redisKey = "temp_token:" + token;

        String mssv;
        try {
            mssv = redisTemplate.execute(
                    validateTempTokenScript,
                    Collections.singletonList(redisKey));
        } catch (Exception e) {
            log.error("Redis error during temp token validation for token: {}", token, e);
            throw new AuthException(AuthErrorCode.TEMP_TOKEN_INVALID);
        }

        if (mssv == null) {
            throw new AuthException(AuthErrorCode.TEMP_TOKEN_INVALID);
        }

        return mssv;
    }

    @Override
    public void consumeTempToken(String token) {
        tempTokenRepository.findById(token).ifPresent(tempTokenRepository::delete);
    }

    // Helper methods
    private boolean isResendCooldownActive(String email) {
        String key = OTP_RESEND_PREFIX + email;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }

    private void setResendCooldown(String email) {
        String key = OTP_RESEND_PREFIX + email;
        redisTemplate.opsForValue().set(key, "1", resendCooldownSeconds, TimeUnit.SECONDS);
    }

    private void revokeOldOtps(String mssv, String otpType) {
        try {
            Long revokedCount = redisTemplate.execute(
                    revokeOldOtpScript,
                    Collections.singletonList(otpType + ":*"),
                    mssv);
            if (revokedCount != null && revokedCount > 0) {
                log.info("Revoked {} old OTP(s) for mssv: {}", revokedCount, mssv);
            }
        } catch (Exception e) {
            log.error("Error revoking old OTPs for mssv: {}", mssv, e);
            // Don't throw exception, this is just cleanup
        }
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

        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }

        return otp.toString();
    }
}
