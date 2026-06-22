package com.be.service.impl;

import com.be.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@tucuchill.com}")
    private String fromEmail;

    // Use Autowired(required = false) so Spring Boot can boot successfully
    // even if JavaMailSender bean is not created due to missing SMTP configurations.
    @Autowired
    public EmailServiceImpl(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationCode(String toEmail, String code) {
        String subject = "[Tủ cũ chill] - Mã xác nhận đăng ký tài khoản";
        String htmlContent = String.format(
                "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e7dfbd; border-radius: 12px; background-color: #f6f4dd;\">" +
                "  <h2 style=\"color: #b84a25; text-align: center; border-bottom: 2px solid #e7dfbd; padding-bottom: 10px;\">Chào mừng đến với Tủ cũ chill!</h2>" +
                "  <p style=\"font-size: 16px; color: #3f3b2f;\">Cảm ơn bạn đã đăng ký tài khoản trên sàn thương mại thời trang secondhand <b>Tủ cũ chill</b>.</p>" +
                "  <p style=\"font-size: 16px; color: #3f3b2f;\">Mã xác thực của bạn là:</p>" +
                "  <div style=\"text-align: center; margin: 30px 0;\">" +
                "    <span style=\"font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #b84a25; padding: 10px 20px; border: 2px dashed #b84a25; border-radius: 8px; background-color: #ffffff;\">%s</span>" +
                "  </div>" +
                "  <p style=\"font-size: 14px; color: #706b5c;\">Lưu ý: Mã xác nhận này có giá trị trong vòng <b>5 phút</b>. Không chia sẻ mã này với bất kỳ ai.</p>" +
                "  <p style=\"font-size: 14px; color: #706b5c; margin-top: 30px; border-top: 1px solid #e7dfbd; padding-top: 15px; text-align: center;\">Chúc bạn có trải nghiệm mua sắm tuyệt vời!</p>" +
                "</div>",
                code
        );

        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                log.info("Email xác thực đã được gửi tới: {}", toEmail);
            } catch (Exception e) {
                log.warn("Không thể gửi email qua SMTP: {}. Fallback sang log console.", e.getMessage());
            }
        } else {
            log.warn("JavaMailSender không khả dụng do thiếu cấu hình SMTP ở file application.properties. Fallback sang log console.");
        }

        // Always print to console so developers can test easily
        System.out.println("\n==================================================");
        System.out.println("   [EMAIL VERIFICATION OTP]");
        System.out.println("   To:   " + toEmail);
        System.out.println("   Code: " + code);
        System.out.println("   Valid for: 5 minutes");
        System.out.println("==================================================\n");
    }

    @Override
    public void sendForgotPasswordCode(String toEmail, String code) {
        String subject = "[Tủ cũ chill] - Mã xác nhận đặt lại mật khẩu";
        String htmlContent = String.format(
                "<div style=\"font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e7dfbd; border-radius: 12px; background-color: #f6f4dd;\">" +
                "  <h2 style=\"color: #b84a25; text-align: center; border-bottom: 2px solid #e7dfbd; padding-bottom: 10px;\">Khôi phục mật khẩu tài khoản</h2>" +
                "  <p style=\"font-size: 16px; color: #3f3b2f;\">Chào bạn,</p>" +
                "  <p style=\"font-size: 16px; color: #3f3b2f;\">Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản tại <b>Tủ cũ chill</b>. Vui lòng sử dụng mã OTP dưới đây để hoàn tất việc khôi phục mật khẩu:</p>" +
                "  <div style=\"text-align: center; margin: 30px 0;\">" +
                "    <span style=\"font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #b84a25; padding: 10px 20px; border: 2px dashed #b84a25; border-radius: 8px; background-color: #ffffff;\">%s</span>" +
                "  </div>" +
                "  <p style=\"font-size: 14px; color: #706b5c;\">Lưu ý: Mã xác nhận này có giá trị trong vòng <b>5 phút</b>. Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>" +
                "  <p style=\"font-size: 14px; color: #706b5c; margin-top: 30px; border-top: 1px solid #e7dfbd; padding-top: 15px; text-align: center;\">Cảm ơn bạn đã đồng hành cùng Tủ cũ chill!</p>" +
                "</div>",
                code
        );

        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject(subject);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                log.info("Email khôi phục mật khẩu đã được gửi tới: {}", toEmail);
            } catch (Exception e) {
                log.warn("Không thể gửi email khôi phục mật khẩu qua SMTP: {}. Fallback sang log console.", e.getMessage());
            }
        } else {
            log.warn("JavaMailSender không khả dụng do thiếu cấu hình SMTP ở file application.properties. Fallback sang log console.");
        }

        // Always print to console so developers can test easily
        System.out.println("\n==================================================");
        System.out.println("   [FORGOT PASSWORD RESET OTP]");
        System.out.println("   To:   " + toEmail);
        System.out.println("   Code: " + code);
        System.out.println("   Valid for: 5 minutes");
        System.out.println("==================================================\n");
    }
}

