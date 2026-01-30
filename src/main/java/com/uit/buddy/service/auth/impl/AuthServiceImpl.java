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
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.mapper.auth.AuthMapper;
import com.uit.buddy.repository.auth.UserRepository;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;
    private final AuthMapper authMapper;

    @Transactional
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

    public TempTokenResponse verifySignupOtp(VerifyOtpRequest request) {

        String mssv = request.getMssv();
        String otp = request.getOtp();

        log.info("Verifying signup OTP for mssv: {}", mssv);

        if (!mssv.matches("^[0-9]{8,10}$")) {
            throw new AuthException(AuthErrorCode.INVALID_MSSV_FORMAT);
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
            throw new AuthException(AuthErrorCode.MSSV_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(email)
                .mssv(mssv)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(null)
                .isVerified(true)
                .build();

        user = userRepository.save(user);
        log.info("User created successfully: {}", email);

        otpService.consumeTempToken(request.getTempToken());

        JwtUserDetails userDetails = new JwtUserDetails(user);
        String accessToken = jwtUtils.generateToken(userDetails);
        Map<String, String> refreshTokenData = jwtUtils.generateRefreshTokenByJwtUserDetails(userDetails, null, false);

        return authMapper.toAuthResponse(user, accessToken, refreshTokenData.get("refresh_token"));
    }

    @Transactional
    public AuthResponse signIn(SignInRequest request) {
        log.info("Sign in attempt for mssv: {}", request.getMssv());

        User user = userRepository.findByMssv(request.getMssv())
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        if (!user.getIsVerified()) {
            throw new AuthException(AuthErrorCode.ACCOUNT_NOT_ACTIVATED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        user.updateLastLogin();
        userRepository.save(user);

        log.info("User signed in successfully: {}", user.getEmail());

        JwtUserDetails userDetails = new JwtUserDetails(user);
        String accessToken = jwtUtils.generateToken(userDetails);
        Map<String, String> refreshTokenData = jwtUtils.generateRefreshTokenByJwtUserDetails(userDetails, null, false);

        return authMapper.toAuthResponse(user, accessToken, refreshTokenData.get("refresh_token"));
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String mssv = jwtUtils.extractMssv(refreshToken);
        User user = userRepository.findByMssv(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtUtils.generateAccessTokenByRefreshToken(refreshToken);

        JwtUserDetails userDetails = new JwtUserDetails(user);

        String familyToken = jwtUtils.extractClaim(refreshToken, claims -> claims.get("family_token", String.class));

        Map<String, String> refreshTokenData = jwtUtils.generateRefreshTokenByJwtUserDetails(userDetails, familyToken,
                false);

        return authMapper.toAuthResponse(user, newAccessToken, refreshTokenData.get("refresh_token"));
    }

    public void initiatePasswordReset(ForgotPasswordRequest request) {
        String mssv = request.getMssv();
        log.info("Initiating password reset for: {}", mssv);

        User user = userRepository.findByMssv(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

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

        jwtUtils.revokeAllRefreshTokensByMssv(user.getMssv());

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
