package com.uit.buddy.service.email.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.uit.buddy.service.email.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendOtpEmail(String email, String otp) {
        String subject = "UIT Buddy - Mã xác thực OTP";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2563eb;">UIT Buddy - Xác thực tài khoản</h2>
                    <p>Xin chào,</p>
                    <p>Mã OTP của bạn là:</p>
                    <div style="background-color: #f3f4f6; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #1f2937;">%s</span>
                    </div>
                    <p>Mã này sẽ hết hạn sau <strong>5 phút</strong>.</p>
                    <p style="color: #6b7280; font-size: 14px;">Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                    <p style="color: #9ca3af; font-size: 12px;">© 2026 UIT Buddy. All rights reserved.</p>
                </div>
                """
                .formatted(otp);

        sendHtmlEmail(email, subject, htmlContent);
        log.info("OTP email sent to: {}", email);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String email, String fullName) {
        String subject = "Chào mừng bạn đến với UIT Buddy!";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2563eb;">Chào mừng đến với UIT Buddy! 🎉</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Cảm ơn bạn đã đăng ký tài khoản UIT Buddy. Chúng tôi rất vui được chào đón bạn!</p>
                    <p>Với UIT Buddy, bạn có thể:</p>
                    <ul>
                        <li>Quản lý thời khóa biểu</li>
                        <li>Theo dõi điểm số</li>
                        <li>Kết nối với bạn bè cùng trường</li>
                    </ul>
                    <p>Hãy bắt đầu khám phá ngay!</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                    <p style="color: #9ca3af; font-size: 12px;">© 2026 UIT Buddy. All rights reserved.</p>
                </div>
                """.formatted(fullName != null ? fullName : "bạn");

        sendHtmlEmail(email, subject, htmlContent);
        log.info("Welcome email sent to: {}", email);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String email, String otp) {
        String subject = "UIT Buddy - Đặt lại mật khẩu";
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2563eb;">UIT Buddy - Đặt lại mật khẩu</h2>
                    <p>Xin chào,</p>
                    <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                    <p>Mã OTP của bạn là:</p>
                    <div style="background-color: #fef3c7; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0; border: 1px solid #f59e0b;">
                        <span style="font-size: 32px; font-weight: bold; letter-spacing: 8px; color: #92400e;">%s</span>
                    </div>
                    <p>Mã này sẽ hết hạn sau <strong>5 phút</strong>.</p>
                    <p style="color: #dc2626; font-size: 14px;">⚠️ Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và đảm bảo tài khoản của bạn an toàn.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                    <p style="color: #9ca3af; font-size: 12px;">© 2026 UIT Buddy. All rights reserved.</p>
                </div>
                """
                .formatted(otp);

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
}
