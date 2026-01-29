package com.uit.buddy.service.auth.impl;

import com.uit.buddy.entity.redis.PasswordResetToken;
import com.uit.buddy.entity.redis.SignUpToken;
import com.uit.buddy.entity.redis.TempToken;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.repository.redis.PasswordResetTokenRepository;
import com.uit.buddy.repository.redis.SignUpTokenRepository;
import com.uit.buddy.repository.redis.TempTokenRepository;
import com.uit.buddy.service.auth.OtpService;
import com.uit.buddy.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final SignUpTokenRepository signUpTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TempTokenRepository tempTokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRATION_SECONDS = 300; // 5 minutes
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long RESEND_COOLDOWN_SECONDS = 120; // 2 minutes
    private static final long TEMP_TOKEN_EXPIRATION_SECONDS = 600; // 10 minutes
    private static final String OTP_RESEND_PREFIX = "otp_resend:";

    @Override
    public void sendForgetPasswordOtp(String email) {
        if (isResendCooldownActive(email)) {
            throw new AuthException(AuthErrorCode.TOO_MANY_OTP_REQUESTS);
        }

        String otp = generateOtp();
        String mssv = email.replace("@gm.uit.edu.vn", "");

        PasswordResetToken token = PasswordResetToken.builder()
                .otp(otp)
                .mssv(mssv)
                .attempts(0)
                .isRevoked(false)
                .ttl(OTP_EXPIRATION_SECONDS)
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

        PasswordResetToken token = passwordResetTokenRepository.findByMssvAndIsRevoked(mssv, false)
                .orElseThrow(() -> new AuthException(AuthErrorCode.OTP_NOT_FOUND));

        if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
            passwordResetTokenRepository.delete(token);
            throw new AuthException(AuthErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (!token.getOtp().equals(otp)) {
            token.setAttempts(token.getAttempts() + 1);
            passwordResetTokenRepository.save(token);
            throw new AuthException(AuthErrorCode.OTP_INVALID);
        }

        passwordResetTokenRepository.delete(token);
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

        String otp = generateOtp();

        SignUpToken token = SignUpToken.builder()
                .otp(otp)
                .mssv(mssv)
                .attempts(0)
                .isRevoked(false)
                .ttl(OTP_EXPIRATION_SECONDS)
                .build();

        signUpTokenRepository.save(token);
        setResendCooldown(email);

        emailService.sendOtpEmail(email, otp);
        log.info("Signup OTP sent to: {}", email);
    }

    @Override
    public String verifySignupOtp(String mssv, String otp) {
        SignUpToken token = signUpTokenRepository.findByMssvAndIsRevoked(mssv, false)
                .orElseThrow(() -> new AuthException(AuthErrorCode.OTP_NOT_FOUND));

        if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
            signUpTokenRepository.delete(token);
            throw new AuthException(AuthErrorCode.OTP_MAX_ATTEMPTS);
        }

        if (!token.getOtp().equals(otp)) {
            token.setAttempts(token.getAttempts() + 1);
            signUpTokenRepository.save(token);
            throw new AuthException(AuthErrorCode.OTP_INVALID);
        }

        signUpTokenRepository.delete(token);

        // Generate temp token
        String tempTokenValue = generateTempToken();
        TempToken tempToken = TempToken.builder()
                .token(tempTokenValue)
                .mssv(mssv)
                .isRevoked(false)
                .ttl(TEMP_TOKEN_EXPIRATION_SECONDS)
                .build();

        tempTokenRepository.save(tempToken);

        log.info("Signup OTP verified for mssv: {}, temp token generated", mssv);
        return tempTokenValue;
    }

    @Override
    public String validateTempToken(String token) {
        TempToken tempToken = tempTokenRepository.findById(token)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TEMP_TOKEN_INVALID));

        if (tempToken.isRevoked()) {
            throw new AuthException(AuthErrorCode.TEMP_TOKEN_INVALID);
        }

        return tempToken.getMssv();
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
        redisTemplate.opsForValue().set(key, "1", RESEND_COOLDOWN_SECONDS, TimeUnit.SECONDS);
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
