package com.uit.buddy.controller.auth;

import com.uit.buddy.controller.AbstractBaseController;
import com.uit.buddy.dto.base.SingleResponse;
import com.uit.buddy.dto.base.SuccessResponse;
import com.uit.buddy.dto.request.auth.CompleteSignUpRequest;
import com.uit.buddy.dto.request.auth.ForgetPasswordRequest;
import com.uit.buddy.dto.request.auth.ResetPasswordRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.ValidateTokenRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.dto.response.auth.ValidateTokenResponse;
import com.uit.buddy.exception.auth.AuthErrorCode;
import com.uit.buddy.exception.auth.AuthException;
import com.uit.buddy.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentication",
    description = "Endpoints for user authentication and session management")
public class AuthController extends AbstractBaseController {

  private final AuthService authService;

  @PostMapping("/signup/init")
  @Operation(
      summary = "Validate Moodle Token",
      description = "Check if the provided wstoken is valid and extract student info")
  public ResponseEntity<SingleResponse<ValidateTokenResponse>> validateToken(
      @Valid @RequestBody ValidateTokenRequest request) {
    ValidateTokenResponse response = authService.initSignUp(request);
    return successSingle(
        response, "Token validated successfully. Please complete signup within 5 minutes.");
  }

  @PostMapping("/signup/complete")
  @Operation(
      summary = "Complete Sign Up",
      description = "Finalize the registration process and create a new student account")
  public ResponseEntity<SingleResponse<AuthResponse>> completeSignUp(
      @Valid @RequestBody CompleteSignUpRequest request) {
    AuthResponse response = authService.completeSignUp(request);
    return successSingle(response, "Sign up successful!");
  }

  @PostMapping("/signin")
  @Operation(summary = "Sign In", description = "Authenticate student using MSSV and password")
  public ResponseEntity<SingleResponse<AuthResponse>> signIn(
      @Valid @RequestBody SignInRequest request) {
    AuthResponse response = authService.signIn(request);
    return successSingle(response, "Sign in successful!");
  }

  @PostMapping("/forget-password")
  @Operation(
      summary = "Forget Password",
      description = "Initiate password reset process by sending an OTP to the student's email")
  public ResponseEntity<SuccessResponse> forgetPassword(
      @Valid @RequestBody ForgetPasswordRequest request) {
    authService.forgetPassword(request);
    return success("Password reset OTP sent to your email!");
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reset Password", description = "Verify OTP and reset password")
  public ResponseEntity<SuccessResponse> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    if (!request.newPassword().equals(request.confirmPassword())) {
      throw new AuthException(AuthErrorCode.PASSWORD_MISMATCH);
    }
    authService.resetPassword(request.mssv(), request.otpCode(), request.newPassword());
    return success("Password reset successfully! Please sign in with your new password.");
  }

  @PostMapping("/refresh-token")
  @Operation(
      summary = "Refresh Token",
      description = "Obtain a new access token using a valid refresh token")
  public ResponseEntity<SingleResponse<AuthResponse>> refreshToken(
      @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
    }
    AuthResponse response = authService.refreshToken(refreshToken);
    return successSingle(response, "Token refreshed successfully!");
  }

  @PostMapping("/signout")
  @Operation(summary = "Sign Out", description = "Logout the user and invalidate the refresh token")
  public ResponseEntity<SuccessResponse> signOut(
      @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
    }
    authService.signOut(refreshToken);
    return success("Sign out successfully!");
  }
}
