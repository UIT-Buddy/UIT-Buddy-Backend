package com.uit.buddy.service.auth;

import com.uit.buddy.dto.request.auth.CompleteSignUpRequest;
import com.uit.buddy.dto.request.auth.ForgetPasswordRequest;
import com.uit.buddy.dto.request.auth.SignInRequest;
import com.uit.buddy.dto.request.auth.ValidateTokenRequest;
import com.uit.buddy.dto.response.auth.AuthResponse;
import com.uit.buddy.dto.response.auth.ValidateTokenResponse;

public interface AuthService {

    ValidateTokenResponse initSignUp(ValidateTokenRequest request);

    AuthResponse completeSignUp(CompleteSignUpRequest request);

    AuthResponse signIn(SignInRequest request);

    void forgetPassword(ForgetPasswordRequest request);

    void resetPassword(String mssv, String otpCode, String newPassword);

    AuthResponse refreshToken(String refreshToken);

    void signOut(String refreshToken);

    String getDecryptedWstoken(String mssv);
}
