package com.uit.buddy.service.auth.impl;

import com.uit.buddy.dto.request.auth.ChangePasswordRequest;
import com.uit.buddy.dto.request.auth.ForgotPasswordRequest;
import com.uit.buddy.dto.request.auth.PasswordSettingRequest;
import com.uit.buddy.dto.request.auth.ResetPasswordRequest;
import com.uit.buddy.dto.request.auth.VerifyOtpRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.SignUpRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.dto.response.auth.TempTokenResponse;
import com.uit.buddy.entity.auth.User;
import com.uit.buddy.enums.auth.UserRole;
import com.uit.buddy.enums.auth.UserStatus;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.mapper.auth.AuthMapper;
import com.uit.buddy.repository.auth.UserRepository;
import com.uit.buddy.security.JwtUserDetails;
import com.uit.buddy.security.JwtUtils;
import com.uit.buddy.service.auth.AuthService;
import com.uit.buddy.service.auth.OtpService;
import com.uit.buddy.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;
    private final OtpService otpService;
    private final AuthMapper authMapper;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 10;

    @Transactional
    public void initiateSignUp(SignUpRequest request) {
        String mssv = request.getMssv();
        String email = mssv + "@gm.uit.edu.vn";

        log.info("Initiating signup for mssv: {}", mssv);

        if (!mssv.matches("^[0-9]{8,10}$")) {
            throw new AuthException(AuthErrorCode.INVALID_mssv_FORMAT);
        }

        if (userRepository.existsByMssv(mssv)) {
            throw new AuthException(AuthErrorCode.mssv_ALREADY_EXISTS);
        }
        otpService.sendSignupOtp(mssv);
        log.info("OTP sent to: {}", email);
    }

    public TempTokenResponse verifySignupOtp(VerifyOtpRequest request) {

        String mssv = request.getMssv();
        String otp = request.getOtp();

        log.info("Verifying signup OTP for mssv: {}", mssv);

        if (!mssv.matches("^[0-9]{8,10}$")) {
            throw new AuthException(AuthErrorCode.INVALID_mssv_FORMAT);
        }

        String tempToken = otpService.verifySignupOtp(mssv, otp);
        String email = mssv + "@gm.uit.edu.vn";

        return TempTokenResponse.builder()
                .tempToken(tempToken)
                .mssv(mssv)
                .email(email)
                .expiresIn(600) // 10 minutes in seconds
                .build();
    }

    @Transactional
    public AuthResponse completeSignUp(PasswordSettingRequest request) {
        log.info("Completing signup with temp token");

        String mssv = otpService.validateTempToken(request.getTempToken());
        String email = mssv + "@gm.uit.edu.vn";

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (!isPasswordStrong(request.getPassword())) {
            throw new AuthException(AuthErrorCode.WEAK_PASSWORD);
        }

        if (userRepository.existsByMssv(mssv)) {
            otpService.consumeTempToken(request.getTempToken());
            throw new AuthException(AuthErrorCode.mssv_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(email)
                .mssv(mssv)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(null)
                .role(UserRole.STUDENT)
                .status(UserStatus.ACTIVE)
                .isActivated(true)
                .isLocked(false)
                .failedLoginAttempts(0)
                .build();

        user = userRepository.save(user);
        log.info("User created successfully: {}", email);

        otpService.consumeTempToken(request.getTempToken());

        JwtUserDetails userDetails = new JwtUserDetails(user);
        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Save refresh token to Redis
        redisService.saveRefreshToken(user.getId().toString(), refreshToken, refreshExpiration);

        return authMapper.toAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse signIn(SignInRequest request) {
        log.info("Sign in attempt for mssv: {}", request.getMssv());

        User user = userRepository.findByEmail(request.getMssv())
                .or(() -> userRepository.findByMssv(request.getMssv()))
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (user.isAccountLocked()) {
            throw new AuthException(AuthErrorCode.ACCOUNT_LOCKED);
        }

        if (!user.getIsActivated()) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_ACTIVATED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        user.resetFailedLoginAttempts();
        user.updateLastLogin();
        userRepository.save(user);

        log.info("User signed in successfully: {}", user.getEmail());

        JwtUserDetails userDetails = new JwtUserDetails(user);
        String accessToken = jwtUtils.generateToken(userDetails);
        String refreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Save refresh token to Redis
        redisService.saveRefreshToken(user.getId().toString(), refreshToken, refreshExpiration);

        return authMapper.toAuthResponse(user, accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtUtils.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // Verify refresh token from Redis
        String storedToken = redisService.getRefreshToken(user.getId().toString());
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        JwtUserDetails userDetails = new JwtUserDetails(user);

        if (!jwtUtils.isTokenValid(refreshToken, userDetails)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);
        }

        String newAccessToken = jwtUtils.generateToken(userDetails);
        String newRefreshToken = jwtUtils.generateRefreshToken(userDetails);

        // Update refresh token in Redis
        redisService.saveRefreshToken(user.getId().toString(), newRefreshToken, refreshExpiration);

        return authMapper.toAuthResponse(user, newAccessToken, newRefreshToken);
    }

    public void initiatePasswordReset(ForgotPasswordRequest request) {
        String emailOrmssv = request.getMssv();
        log.info("Initiating password reset for: {}", emailOrmssv);

        User user = userRepository.findByEmail(emailOrmssv)
                .or(() -> userRepository.findByMssv(emailOrmssv))
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        // Send OTP
        otpService.sendForgetPasswordOtp(user.getEmail());
        log.info("Password reset OTP sent to: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getMssv());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (!isPasswordStrong(request.getNewPassword())) {
            throw new AuthException(AuthErrorCode.WEAK_PASSWORD);
        }

        User user = userRepository.findByEmail(request.getMssv())
                .or(() -> userRepository.findByMssv(request.getMssv()))
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        otpService.verifyOtp(user.getEmail(), request.getOtp());

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.resetFailedLoginAttempts();
        userRepository.save(user);
        log.info("Password reset successfully for: {}", user.getEmail());
    }

    @Transactional
    private void handleFailedLogin(User user) {
        user.incrementFailedLoginAttempts();

        if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            user.lockAccount(LOCK_DURATION_MINUTES);
            log.warn("Account locked due to too many failed attempts: {}", user.getEmail());
        }

        userRepository.save(user);

        if (user.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            throw new AuthException(AuthErrorCode.TOO_MANY_LOGIN_ATTEMPTS);
        }
    }

    private boolean isPasswordStrong(String password) {
        // 8-50 characters, contains uppercase, lowercase, number, special character
        return password.length() >= 8 &&
                password.length() <= 50 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[@$!%*?&].*");
    }

    public AuthResponse.UserInfo getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        return authMapper.toUserInfo(user);
    }

    @Transactional
    public void signOut() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        redisService.deleteRefreshToken(user.getId().toString());
        log.info("User signed out: {}", user.getEmail());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (!isPasswordStrong(request.getNewPassword())) {
            throw new AuthException(AuthErrorCode.WEAK_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for: {}", user.getEmail());
    }
}
