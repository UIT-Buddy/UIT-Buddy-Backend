package com.uit.buddy.controller.auth;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.auth.ChangePasswordRequest;
import com.uit.buddy.dto.request.auth.ForgotPasswordRequest;
import com.uit.buddy.dto.request.auth.PasswordSettingRequest;
import com.uit.buddy.dto.request.auth.ResetPasswordRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.SignUpRequest;
import com.uit.buddy.dto.request.auth.VerifyOtpRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.dto.response.auth.TempTokenResponse;
import com.uit.buddy.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController extends AbstractBaseController {

    private final AuthService authService;

    @PostMapping("/signup/initiate")
    public ResponseEntity<SuccessResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.initiateSignUp(request);
        return success("OTP has been sent to your student email. Please check your inbox!");
    }

    @PostMapping("/signup/verify-otp")
    public ResponseEntity<SingleResponse<TempTokenResponse>> verifySignupOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        TempTokenResponse response = authService.verifySignupOtp(request);
        return successSingle(response, "OTP verified! Please set your password within 10 minutes.");
    }

    @PostMapping("/signup/set-password")
    public ResponseEntity<SingleResponse<AuthResponse>> setPassword(
            @Valid @RequestBody PasswordSettingRequest request,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.completeSignUp(request);
        httpResponse.setHeader("X-Refresh-Token", response.getRefreshToken());
        return successSingle(response, "Registration completed successfully!");
    }

    @PostMapping("/signin")
    public ResponseEntity<SingleResponse<AuthResponse>> signIn(
            @Valid @RequestBody SignInRequest request,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.signIn(request);
        httpResponse.setHeader("X-Refresh-Token", response.getRefreshToken());
        return successSingle(response, "Sign-in successful!");
    }

    @PostMapping("/signup/resend-otp")
    @Operation(description = "Can be requested once every 2 minutes")
    public ResponseEntity<SuccessResponse> resendSignupOtp(@Valid @RequestBody SignUpRequest request) {
        authService.initiateSignUp(request);
        return success("A new OTP has been sent to your email.");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<SingleResponse<AuthResponse>> refreshToken(
            @RequestHeader("X-Refresh-Token") String refreshToken,
            HttpServletResponse httpResponse) {
        AuthResponse response = authService.refreshToken(refreshToken);
        httpResponse.setHeader("X-Refresh-Token", response.getRefreshToken());
        return successSingle(response, "Token refreshed successfully!");
    }

    @PostMapping("/signout")
    public ResponseEntity<SuccessResponse> signOut(HttpServletResponse httpResponse) {
        authService.signOut();
        return success("Sign-out successful!");
    }

    @GetMapping("/me")
    public ResponseEntity<SingleResponse<AuthResponse.UserInfo>> getCurrentUser() {
        AuthResponse.UserInfo userInfo = authService.getUserInfo();
        return successSingle(userInfo, "User information retrieved successfully.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.initiatePasswordReset(request);
        return success("OTP has been sent. Please check your email!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<SuccessResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return success("Password has been reset. Please sign in with your new credentials!");
    }

    @PostMapping("/change-password")
    public ResponseEntity<SuccessResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return success("Password changed successfully!");
    }
}
