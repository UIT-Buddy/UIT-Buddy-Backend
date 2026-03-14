package com.uit.buddy.service.email.impl;

import com.uit.buddy.service.email.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Override
  @Async
  public void sendPasswordResetOtp(String toEmail, String otpCode, long expirationMinutes) {
    try {
      String subject = "Mã OTP đặt lại mật khẩu - UIT Buddy";
      String htmlContent =
          loadTemplate("templates/email/password-reset-otp.html")
              .replace("{{otpCode}}", otpCode)
              .replace("{{expirationMinutes}}", String.valueOf(expirationMinutes));

      sendHtmlEmail(toEmail, subject, htmlContent);
      log.info("Password reset OTP email sent to: {}", toEmail);
    } catch (Exception e) {
      log.error("Failed to send password reset OTP email to {}: {}", toEmail, e.getMessage());
      throw new RuntimeException("Failed to send email", e);
    }
  }

  private void sendHtmlEmail(String to, String subject, String htmlContent)
      throws MessagingException {
    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

    helper.setTo(to);
    helper.setSubject(subject);
    helper.setText(htmlContent, true);

    mailSender.send(message);
  }

  private String loadTemplate(String templatePath) throws IOException {
    ClassPathResource resource = new ClassPathResource(templatePath);
    return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }
}
