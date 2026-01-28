package com.uit.buddy.service.auth;

import com.uit.buddy.dto.request.auth.ChangePasswordRequest;
import com.uit.buddy.dto.request.auth.ForgotPasswordRequest;
import com.uit.buddy.dto.request.auth.PasswordSettingRequest;
import com.uit.buddy.dto.request.auth.ResetPasswordRequest;
import com.uit.buddy.dto.request.auth.VerifyOtpRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.SignUpRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.dto.response.auth.TempTokenResponse;

public interface AuthService {

    void initiateSignUp(SignUpRequest request);

    TempTokenResponse verifySignupOtp(VerifyOtpRequest request);

    AuthResponse completeSignUp(PasswordSettingRequest request);

    AuthResponse signIn(SignInRequest request);

    AuthResponse refreshToken(String refreshToken);

    void signOut();

    AuthResponse.UserInfo getUserInfo();

    void initiatePasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
