package com.uit.buddy.service.auth.impl;

import com.uit.buddy.dto.request.auth.ChangePasswordRequest;
import com.uit.buddy.dto.request.auth.ForgotPasswordRequest;
import com.uit.buddy.dto.request.auth.CompleteSignUpRequest;
import com.uit.buddy.dto.request.auth.ResetPasswordRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.SignUpRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.entity.auth.PendingAccount;
import com.uit.buddy.entity.user.User;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.mapper.auth.AuthMapper;
import com.uit.buddy.repository.redis.PendingAccountRepository;
import com.uit.buddy.repository.user.UserRepository;
import com.uit.buddy.security.JwtUserDetails;
import com.uit.buddy.security.JwtUtils;
import com.uit.buddy.service.auth.AuthService;
import com.uit.buddy.service.auth.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PendingAccountRepository pendingAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;
    private final AuthMapper authMapper;

    @Override
    public void initiateSignUp(SignUpRequest request) {
        String mssv = request.getMssv();
        String email = mssv + "@gm.uit.edu.vn";

        log.info("Initiating signup for mssv: {}", mssv);

        if (!mssv.matches("^[0-9]{8,10}$")) {
            throw new AuthException(AuthErrorCode.INVALID_MSSV_FORMAT);
        }

        if (userRepository.existsByMssv(mssv)) {
            throw new AuthException(AuthErrorCode.MSSV_ALREADY_EXISTS);
        }
        otpService.sendSignupOtp(mssv);
        log.info("OTP sent to: {}", email);
    }

    @Override
    @Transactional
    public AuthResponse completeSignUp(CompleteSignUpRequest request) {
        String mssv = request.getMssv();
        String otp = request.getOtp();
        String email = mssv + "@gm.uit.edu.vn";

        log.info("Completing signup for mssv: {}", mssv);

        if (!mssv.matches("^[0-9]{8,10}$")) {
            throw new AuthException(AuthErrorCode.INVALID_MSSV_FORMAT);
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (!isPasswordStrong(request.getPassword())) {
            throw new AuthException(AuthErrorCode.WEAK_PASSWORD);
        }

        PendingAccount pendingAccount = pendingAccountRepository.findByMssv(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.PENDING_ACCOUNT_NOT_FOUND));

        if (pendingAccount.isRevoked()) {
            throw new AuthException(AuthErrorCode.PENDING_ACCOUNT_EXPIRED);
        }

        if (userRepository.existsByMssv(mssv)) {
            pendingAccountRepository.deleteById(mssv);
            throw new AuthException(AuthErrorCode.MSSV_ALREADY_EXISTS);
        }

        otpService.verifySignupOtp(mssv, otp);

        User user = User.builder()
                .email(email)
                .mssv(mssv)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(null)
                .build();

        user = userRepository.save(user);
        log.info("User created successfully: {}", email);

        pendingAccountRepository.deleteById(mssv);
        log.info("Pending account deleted for mssv: {}", mssv);

        JwtUserDetails userDetails = new JwtUserDetails(user);
        String accessToken = jwtUtils.generateToken(userDetails);
        Map<String, String> refreshTokenData = jwtUtils.generateRefreshTokenByJwtUserDetails(userDetails, false);

        return authMapper.toAuthResponse(user, accessToken, refreshTokenData.get("refresh_token"));
    }

    @Override
    @Transactional
    public AuthResponse signIn(SignInRequest request) {
        log.info("Sign in attempt for mssv: {}", request.getMssv());

        User user = userRepository.findByMssv(request.getMssv())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        user.updateLastLogin();
        userRepository.save(user);

        log.info("User signed in successfully: {}", user.getEmail());

        JwtUserDetails userDetails = new JwtUserDetails(user);
        String accessToken = jwtUtils.generateToken(userDetails);
        Boolean rememberMe = request.getRememberMe() != null ? request.getRememberMe() : false;
        Map<String, String> refreshTokenData = jwtUtils.generateRefreshTokenByJwtUserDetails(userDetails, rememberMe);

        return authMapper.toAuthResponse(user, accessToken, refreshTokenData.get("refresh_token"));
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String mssv = jwtUtils.extractMssv(refreshToken);
        User user = userRepository.findByMssv(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtils.generateAccessTokenByRefreshToken(refreshToken);

        JwtUserDetails userDetails = new JwtUserDetails(user);

        Map<String, String> refreshTokenData = jwtUtils.generateRefreshTokenByJwtUserDetails(userDetails, false);

        return authMapper.toAuthResponse(user, newAccessToken, refreshTokenData.get("refresh_token"));
    }

    @Override
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        String mssv = request.getMssv();
        log.info("Initiating password reset for: {}", mssv);

        User user = userRepository.findByMssv(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        otpService.sendForgetPasswordOtp(user.getEmail());
        log.info("Password reset OTP sent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getMssv());

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (!isPasswordStrong(request.getNewPassword())) {
            throw new AuthException(AuthErrorCode.WEAK_PASSWORD);
        }

        User user = userRepository.findByMssv(request.getMssv())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        otpService.verifyOtp(user.getEmail(), request.getOtp());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset successfully for: {}", user.getEmail());
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

    @Override
    public AuthResponse.UserInfo getUserInfo() {
        String mssv = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByMssv(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));
        return authMapper.toUserInfo(user);
    }

    @Override
    public void signOut() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        jwtUtils.revokeAllRefreshTokensByMssv(user.getMssv());

        log.info("User signed out: {}", user.getEmail());
    }

    @Override
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
