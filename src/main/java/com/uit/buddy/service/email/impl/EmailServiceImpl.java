package com.uit.buddy.service.email.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.InputStream;

import com.uit.buddy.service.email.EmailService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendOtpEmail(String email, String otp) {
        String subject = "UIT Buddy - Mã xác thực OTP";
        String htmlContent = loadTemplate("templates/email/otp-email.html")
                .replace("{{OTP}}", otp);

        sendHtmlEmail(email, subject, htmlContent);
        log.info("OTP email sent to: {}", email);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String email, String fullName) {
        String subject = "Chào mừng bạn đến với UIT Buddy!";
        String htmlContent = loadTemplate("templates/email/welcome-email.html")
                .replace("{{FULL_NAME}}", fullName != null ? fullName : "bạn");

        sendHtmlEmail(email, subject, htmlContent);
        log.info("Welcome email sent to: {}", email);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String otp) {
        String subject = "UIT Buddy - Đặt lại mật khẩu";
        String htmlContent = loadTemplate("templates/email/password-reset-email.html")
                .replace("{{OTP}}", otp);

        sendHtmlEmail(email, subject, htmlContent);
        log.info("Password reset email sent to: {}", email);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }

    private String loadTemplate(String templatePath) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            try (InputStream inputStream = resource.getInputStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templatePath, e);
            throw new RuntimeException("Không thể tải template email.");
        }
    }

}
