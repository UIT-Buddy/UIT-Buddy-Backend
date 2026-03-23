package com.uit.buddy.service.auth.impl;

import com.uit.buddy.client.CometChatClient;
import com.uit.buddy.client.UitClient;
import com.uit.buddy.constant.AppConstants;
import com.uit.buddy.dto.request.auth.CompleteSignUpRequest;
import com.uit.buddy.dto.request.auth.ForgetPasswordRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.ValidateTokenRequest;
import com.uit.buddy.dto.request.client.CometChatUserRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.dto.response.auth.StudentResponse;
import com.uit.buddy.dto.response.auth.ValidateTokenResponse;
import com.uit.buddy.dto.response.client.EnrolledCourseResponse;
import com.uit.buddy.dto.response.client.SiteInfoResponse;
import com.uit.buddy.entity.academic.HomeClass;
import com.uit.buddy.entity.redis.PasswordResetOtp;
import com.uit.buddy.entity.redis.PendingAccount;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.entity.user.UserSetting;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.exception.client.ExternalClientException;
import com.uit.buddy.exception.homeclass.HomeClassErrorCode;
import com.uit.buddy.exception.homeclass.HomeClassException;
import com.uit.buddy.mapper.user.StudentMapper;
import com.uit.buddy.repository.academic.HomeClassRepository;
import com.uit.buddy.repository.academic.MajorRepository;
import com.uit.buddy.repository.auth.PasswordResetTokenRepository;
import com.uit.buddy.repository.auth.PendingAccountRepository;
import com.uit.buddy.repository.auth.RefreshTokenRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.security.JwtUtils;
import com.uit.buddy.service.auth.AuthService;
import com.uit.buddy.service.cloudinary.CloudinaryService;
import com.uit.buddy.service.email.EmailService;
import com.uit.buddy.service.encryption.WsTokenEncryptionService;
import com.uit.buddy.service.fcm.FcmService;
import com.uit.buddy.util.OtpUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final StudentRepository studentRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PendingAccountRepository pendingAccountRepository;
    private final UitClient uitClient;
    private final CometChatClient cometChatClient;
    private final HomeClassRepository homeClassRepository;
    private final MajorRepository majorRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StudentMapper studentMapper;
    private final EmailService emailService;
    private final WsTokenEncryptionService wsTokenEncryptionService;
    private final FcmService fcmService;
    private final OtpUtils otpUtils;
    private final CloudinaryService cloudinaryService;

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.expiration-seconds}")
    private long otpExpirationSeconds;

    @Value("${app.otp.max-attempts}")
    private int otpMaxAttempts;

    @Value("${app.pending-account.expiration-seconds}")
    private long pendingAccountExpirationSeconds;

    @Override
    @Transactional
    public ValidateTokenResponse initSignUp(ValidateTokenRequest request) {
        log.info("Validating token for authentication");

        SiteInfoResponse siteInfo = validateMoodleToken(request.wstoken());
        String mssv = siteInfo.username();

        Student existingStudent = studentRepository.findById(mssv).orElse(null);
        if (existingStudent != null) {
            log.info("Student {} already exists, silent syncing Moodle data", mssv);
            syncExistingStudent(existingStudent, request.wstoken(), siteInfo.fullname());
            throw new AuthException(AuthErrorCode.STUDENT_ALREADY_EXISTS);
        }

        pendingAccountRepository.findById(mssv).ifPresent(oldAccount -> {
            log.info("Deleting old pending account for MSSV: {}", mssv);
            pendingAccountRepository.delete(oldAccount);
        });

        String homeClassCode = extractHomeClassCode(request.wstoken(), siteInfo.userid(), mssv);

        if (homeClassCode == null || homeClassCode.isBlank()) {
            log.error("HomeClassCode is null or empty for MSSV: {}", mssv);
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS,
                    "Cannot determine your home class from Moodle. Please contact support.");
        }

        String signupToken = UUID.randomUUID().toString();
        log.info("Generated signup token for MSSV: {}", mssv);

        PendingAccount pendingAccount = PendingAccount.builder().mssv(mssv).signupToken(signupToken)
                .encryptedWstoken(wsTokenEncryptionService.encryptWstoken(request.wstoken()))
                .fullName(siteInfo.fullname()).homeClassCode(homeClassCode).ttl(pendingAccountExpirationSeconds)
                .build();

        pendingAccountRepository.save(pendingAccount);
        log.info("Created pending account for MSSV: {} with homeClassCode: {}", mssv, homeClassCode);

        return new ValidateTokenResponse(signupToken, mssv, siteInfo.fullname());
    }

    @Override
    @Transactional
    public AuthResponse completeSignUp(CompleteSignUpRequest request) {
        log.info("Complete sign up attempt for MSSV: {}", request.mssv());

        if (!request.password().equals(request.confirmPassword())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        if (studentRepository.existsById(request.mssv())) {
            throw new AuthException(AuthErrorCode.STUDENT_ALREADY_EXISTS);
        }

        String avatarUrl = cloudinaryService.createDefaultAvatar(request.mssv());

        PendingAccount pendingAccount = pendingAccountRepository.findById(request.mssv())
                .orElseThrow(() -> new AuthException(AuthErrorCode.PENDING_ACCOUNT_NOT_FOUND));

        if (!pendingAccount.getSignupToken().equals(request.signupToken())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS,
                    "Invalid signup token. Please validate your token again.");
        }

        if (!pendingAccount.getMssv().equals(request.mssv())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS, "Invalid signup request. MSSV does not match.");
        }

        if (pendingAccount.getHomeClassCode() == null || pendingAccount.getHomeClassCode().isBlank()) {
            log.error("HomeClassCode is null in pending account for MSSV: {}", request.mssv());
            pendingAccountRepository.delete(pendingAccount);
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS,
                    "Invalid account data. Please validate your token again.");
        }

        createCometChatUser(request.mssv(), pendingAccount.getFullName(), avatarUrl);
        String cometAuthToken = createCometChatAuthToken(request.mssv());

        String homeClassCode = pendingAccount.getHomeClassCode();
        ensureHomeClassExists(homeClassCode, request.mssv());

        Student student = Student.builder().mssv(request.mssv()).fullName(pendingAccount.getFullName())
                .email(request.mssv() + AppConstants.STUDENT_EMAIL_DOMAIN).avatarUrl(avatarUrl).bio(null)
                .cometUid(request.mssv()).encryptedWstoken(pendingAccount.getEncryptedWstoken())
                .cometAuthToken(cometAuthToken)
                .password(passwordEncoder.encode(request.password())).homeClassCode(homeClassCode).build();

        UserSetting userSetting = UserSetting.builder().mssv(request.mssv()).enableNotification(true)
                .enableScheduleReminder(true).student(student).build();

        student.setUserSetting(userSetting);

        try {
            studentRepository.save(student);
            log.info("Successfully created student with UserSetting for MSSV: {}", request.mssv());
            if (request.fcmToken() != null && !request.fcmToken().isBlank()) {
                fcmService.syncDeviceToken(request.mssv(), request.fcmToken());
            }
            log.info("Successfully created student: {} with homeClassCode: {}", request.mssv(), homeClassCode);
        } catch (Exception e) {
            log.error("Failed to save student: {}", request.mssv(), e);

            // Rollback CometChat user
            try {
                cometChatClient.deleteUser(request.mssv());
                log.info("Successfully rolled back CometChat user for MSSV: {}", request.mssv());
            } catch (Exception ex) {
                log.error("Failed to rollback CometChat user for MSSV: {}", request.mssv(), ex);
            }

            // Rollback Cloudinary avatar
            try {
                cloudinaryService.deleteAvatar(request.mssv());
                log.info("Successfully rolled back Cloudinary avatar for MSSV: {}", request.mssv());
            } catch (Exception ex) {
                log.error("Failed to rollback Cloudinary avatar for MSSV: {}", request.mssv(), ex);
            }

            throw new AuthException(AuthErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to create account. Please try again.");
        }

        pendingAccountRepository.delete(pendingAccount);

        String accessToken = jwtUtils.generateAccessToken(request.mssv());
        String refreshToken = jwtUtils.generateRefreshToken(request.mssv(), false);

        StudentResponse studentResponse = studentMapper.toStudentResponse(student);

        return new AuthResponse(accessToken, refreshToken, studentResponse, student.getCometAuthToken());
    }

    @Override
    @Transactional
    public AuthResponse signIn(SignInRequest request) {
        log.info("Sign in attempt for MSSV: {}", request.mssv());

        Student student = studentRepository.findById(request.mssv())
                .orElseThrow(() -> new AuthException(AuthErrorCode.STUDENT_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), student.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String decryptedWstoken = wsTokenEncryptionService.decryptWstoken(student.getEncryptedWstoken());

        try {
            uitClient.fetchSiteInfo(decryptedWstoken);
        } catch (ExternalClientException e) {
            log.warn("Wstoken invalid for MSSV: {}. User needs to re-authenticate with Moodle", request.mssv());
            throw new AuthException(AuthErrorCode.INVALID_WSTOKEN,
                    "Your Moodle token has expired. Please re-authenticate.");
        }

        if (request.fcmToken() != null && !request.fcmToken().isBlank()) {
            fcmService.syncDeviceToken(request.mssv(), request.fcmToken());
        }

        boolean rememberMe = request.rememberMe() != null && request.rememberMe();

        String accessToken = jwtUtils.generateAccessToken(request.mssv());
        String refreshToken = jwtUtils.generateRefreshToken(request.mssv(), rememberMe);

        StudentResponse studentResponse = studentMapper.toStudentResponse(student);

        return new AuthResponse(accessToken, refreshToken, studentResponse, student.getCometAuthToken());
    }

    @Override
    @Transactional
    public void forgetPassword(ForgetPasswordRequest request) {
        log.info("Forget password request for MSSV: {}", request.mssv());

        Student student = studentRepository.findById(request.mssv())
                .orElseThrow(() -> new AuthException(AuthErrorCode.STUDENT_NOT_FOUND));

        String otpCode = otpUtils.generateNumericOtp(otpLength);

        PasswordResetOtp passwordResetOtp = PasswordResetOtp.builder().mssv(request.mssv())
                .otpCode(passwordEncoder.encode(otpCode)).attempts(0).ttl(otpExpirationSeconds).build();

        passwordResetTokenRepository.save(passwordResetOtp);

        try {
            emailService.sendPasswordResetOtp(student.getEmail(), otpCode, otpExpirationSeconds / 60);
            log.info("Successfully sent OTP to email: {}", student.getEmail());
        } catch (MailException e) {
            log.error("Failed to send OTP email for MSSV: {}", request.mssv(), e);
            throw new AuthException(AuthErrorCode.EMAIL_SEND_FAILED, "Failed to send OTP email. Please try again.");
        } catch (Exception e) {
            log.error("Unexpected error sending OTP email for MSSV: {}", request.mssv(), e);
            throw new AuthException(AuthErrorCode.EMAIL_SEND_FAILED, "Failed to send OTP email");
        }
    }

    @Override
    @Transactional
    public void resetPassword(String mssv, String otpCode, String newPassword) {
        log.info("Resetting password for MSSV: {}", mssv);

        // Verify OTP first
        PasswordResetOtp passwordResetOtp = passwordResetTokenRepository.findById(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.OTP_EXPIRED,
                        "OTP has expired or not found. Please request a new one."));

        if (passwordResetOtp.getAttempts() >= otpMaxAttempts) {
            log.warn("Max OTP attempts exceeded for MSSV: {}", mssv);
            passwordResetTokenRepository.delete(passwordResetOtp);
            throw new AuthException(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED,
                    "Maximum verification attempts exceeded. Please request a new OTP.");
        }

        if (!passwordEncoder.matches(otpCode, passwordResetOtp.getOtpCode())) {
            passwordResetOtp.setAttempts(passwordResetOtp.getAttempts() + 1);
            passwordResetTokenRepository.save(passwordResetOtp);

            int remainingAttempts = otpMaxAttempts - passwordResetOtp.getAttempts();
            log.warn("Invalid OTP for MSSV: {}. Remaining attempts: {}", mssv, remainingAttempts);

            throw new AuthException(AuthErrorCode.OTP_INVALID,
                    String.format("Invalid OTP. %d attempt(s) remaining.", remainingAttempts));
        }

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.STUDENT_NOT_FOUND));

        student.setPassword(passwordEncoder.encode(newPassword));
        studentRepository.save(student);

        passwordResetTokenRepository.delete(passwordResetOtp);

        jwtUtils.revokeAllSessions(mssv);

        log.info("Password reset successfully for MSSV: {}", mssv);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refresh token request");

        if (!jwtUtils.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String mssv = jwtUtils.getMssvFromToken(refreshToken);

        if (!refreshTokenRepository.existsById(refreshToken)) {
            log.warn("Refresh token not found in Redis for user");
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED, "Refresh token has been revoked or expired");
        }

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.STUDENT_NOT_FOUND));

        String newAccessToken = jwtUtils.generateAccessToken(mssv);

        StudentResponse studentResponse = studentMapper.toStudentResponse(student);

        return new AuthResponse(newAccessToken, refreshToken, studentResponse, student.getCometAuthToken());
    }

    @Override
    @Transactional
    public void signOut(String refreshToken) {
        log.info("Sign out attempt");

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
        }

        if (!jwtUtils.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String mssv = jwtUtils.getMssvFromToken(refreshToken);

        jwtUtils.revokeSpecificRefreshToken(refreshToken);
        log.info("Successfully signed out user: {}", mssv);
    }

    @Override
    @Transactional(readOnly = true)
    public String getDecryptedWstoken(String mssv) {
        log.debug("Getting decrypted token for MSSV: {}", mssv);

        Student student = studentRepository.findById(mssv)
                .orElseThrow(() -> new AuthException(AuthErrorCode.STUDENT_NOT_FOUND));

        if (student.getEncryptedWstoken() == null || student.getEncryptedWstoken().isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_WSTOKEN, "Wstoken not found for student");
        }

        return wsTokenEncryptionService.decryptWstoken(student.getEncryptedWstoken());
    }

    private SiteInfoResponse fetchSiteInfo(String wstoken) {
        try {
            return uitClient.fetchSiteInfo(wstoken);
        } catch (RestClientException e) {
            log.error("Failed to connect to Moodle API: {}", e.getMessage(), e);
            throw new AuthException(AuthErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Cannot connect to Moodle. Please try again later.");
        } catch (ExternalClientException e) {
            log.error("Moodle API error: {}", e.getMessage(), e);
            throw new AuthException(AuthErrorCode.INVALID_WSTOKEN, "Invalid wstoken");
        } catch (Exception e) {
            log.error("Unexpected error validating token: {}", e.getMessage(), e);
            throw new AuthException(AuthErrorCode.INVALID_WSTOKEN, "Failed to validate wstoken");
        }
    }

    private void ensureHomeClassExists(String homeClassCode, String mssv) {
        log.debug("Ensuring homeClassCode exists: {}", homeClassCode);

        if (homeClassRepository.existsById(homeClassCode)) {
            return;
        }

        if (homeClassCode == null || homeClassCode.length() < 4) {
            cometChatClient.deleteUser(mssv);
            throw new HomeClassException(HomeClassErrorCode.INVALID_HOME_CLASS_FORMAT, "Mã lớp không hợp lệ.");
        }

        String majorCode = homeClassCode.substring(0, 4);
        if (!majorRepository.existsById(majorCode)) {
            cometChatClient.deleteUser(mssv);
            throw new HomeClassException(HomeClassErrorCode.MAJOR_NOT_FOUND,
                    "Ngành " + majorCode + " chưa có trong hệ thống.");
        }

        String yearStr = homeClassCode.replaceAll("[^0-9]", "");
        String academicYear = yearStr.length() >= 4 ? yearStr.substring(0, 4) : "2024";

        HomeClass newHomeClass = HomeClass.builder().homeClassCode(homeClassCode).majorCode(majorCode)
                .academicYear(academicYear).build();

        homeClassRepository.save(newHomeClass);
        log.info("Successfully created missing HomeClass in DB: {}", homeClassCode);
    }

    private void createCometChatUser(String mssv, String fullName, String avatarUrl) {
        log.debug("[Auth Service] Preparing CometChat request - mssv: {}, fullName: {}, avatarUrl: {}", mssv, fullName,
                avatarUrl);

        try {
            CometChatUserRequest cometChatRequest = new CometChatUserRequest(mssv, fullName != null ? fullName : mssv,
                    avatarUrl);
            log.debug("[Auth Service] CometChat request created: {}", cometChatRequest);

            cometChatClient.createUser(cometChatRequest);
            log.info("[Auth Service] Successfully created CometChat user for MSSV: {}", mssv);
        } catch (ExternalClientException e) {
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                log.warn("[Auth Service] CometChat user already exists for MSSV: {}. Continuing signup...", mssv);
                // User already exists in CometChat, continue with signup
                return;
            }
            log.error("[Auth Service] CometChat integration failed for MSSV: {}. Error: {}", mssv, e.getMessage());
            throw new AuthException(AuthErrorCode.EXTERNAL_SERVICE_ERROR, "Failed to initialize chat service");
        } catch (RestClientException e) {
            log.error("[Auth Service] CometChat connection failed for MSSV: {}. Error: {}", mssv, e.getMessage());
            throw new AuthException(AuthErrorCode.EXTERNAL_SERVICE_ERROR, "Failed to connect to chat service");
        }
    }

    private String createCometChatAuthToken(String mssv) {
        log.debug("[Auth Service] Creating CometChat auth token for MSSV: {}", mssv);

        try {
            var cometAuthResponse = cometChatClient.createCometAuthToken(mssv);
            String authToken = cometAuthResponse.data().authToken();
            log.info("[Auth Service] Successfully created CometChat auth token for MSSV: {}", mssv);
            return authToken;
        } catch (Exception e) {
            log.error("[Auth Service] Failed to create CometChat auth token for MSSV: {}", mssv, e);
            return null;
        }
    }

    private String extractHomeClassCode(String wstoken, Long userid, String mssv) {
        try {
            List<EnrolledCourseResponse> courses = uitClient.getUserCourses(wstoken, userid);
            log.debug("Fetched {} courses for MSSV: {}", courses != null ? courses.size() : 0, mssv);

            if (courses == null || courses.isEmpty()) {
                return null;
            }

            return courses.stream()
                    .filter(course -> course.fullName() != null && course.fullName().toUpperCase().contains("CVHT"))
                    .map(EnrolledCourseResponse::shortName).findFirst().orElse(null);

        } catch (ExternalClientException | RestClientException e) {
            log.error("[Auth Service] Moodle API error for MSSV: {}. Error: {}", mssv, e.getMessage());
            throw new AuthException(AuthErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Failed to retrieve student information from Moodle");
        }
    }

    private SiteInfoResponse validateMoodleToken(String wstoken) {
        SiteInfoResponse siteInfo = fetchSiteInfo(wstoken);

        if (siteInfo.username() == null || siteInfo.username().isBlank()) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS, "Cannot extract MSSV from Moodle response");
        }

        return siteInfo;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncExistingStudent(Student student, String wstoken, String fullName) {
        student.setEncryptedWstoken(wsTokenEncryptionService.encryptWstoken(wstoken));
        student.setFullName(fullName);
        studentRepository.saveAndFlush(student);
        log.info("[Sync] Successfully committed new token for existing student: {}", student.getMssv());
    }
}
