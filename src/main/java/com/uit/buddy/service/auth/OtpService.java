package com.uit.buddy.service.auth;

public interface OtpService {

    void sendForgetPasswordOtp(String email);

    void verifyOtp(String email, String otp);

    long getRemainingCooldown(String email);

    void sendSignupOtp(String mssv);

    void verifySignupOtp(String mssv, String otp);
}
