package com.uit.buddy.service.auth;

public interface OtpService {

    void sendForgetPasswordOtp(String email);

    void verifyOtp(String email, String otp);

    long getRemainingCooldown(String email);

    void sendSignupOtp(String mssv);

    String verifySignupOtp(String mssv, String otp);

    String validateTempToken(String tempToken);

    void consumeTempToken(String tempToken);
}
