package com.uit.buddy.service.email;

public interface EmailService {

    void sendOtpEmail(String email, String otp);

    void sendWelcomeEmail(String email, String fullName);

    void sendPasswordResetEmail(String email, String otp);
}
