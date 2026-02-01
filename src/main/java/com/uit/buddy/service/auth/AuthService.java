package com.uit.buddy.service.auth;

import com.uit.buddy.dto.request.auth.ChangePasswordRequest;
import com.uit.buddy.dto.request.auth.CompleteSignUpRequest;
import com.uit.buddy.dto.request.auth.ForgotPasswordRequest;
import com.uit.buddy.dto.request.auth.ResetPasswordRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.SignUpRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;

public interface AuthService {

    void initiateSignUp(SignUpRequest request);

    AuthResponse signIn(SignInRequest request);

    AuthResponse refreshToken(String refreshToken);

    AuthResponse completeSignUp(CompleteSignUpRequest request);

    void signOut();

    AuthResponse.UserInfo getUserInfo();

    void initiatePasswordReset(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
