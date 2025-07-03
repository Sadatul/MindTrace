package com.sadi.backend.services.impls;

import com.sadi.backend.services.abstractions.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Profile("!test")
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        log.debug("Email sent to {}", to);
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your MindTrace OTP Verification Code");

            String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <h2 style="color: #333;">MindTrace Verification Code</h2>
                        <p style="font-size: 16px; color: #555;">
                            Hi there,
                            <br><br>
                            Your One-Time Password (OTP) for MindTrace is:
                        </p>
                        <div style="font-size: 24px; font-weight: bold; color: #4A90E2; text-align: center; padding: 10px 0;">
                            %s
                        </div>
                        <p style="font-size: 14px; color: #888;">
                            This OTP is valid for 10 minutes. Please do not share it with anyone.
                        </p>
                        <hr style="margin: 30px 0;">
                        <p style="font-size: 12px; color: #aaa; text-align: center;">
                            &copy; %d MindTrace. All rights reserved.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(otp, java.time.Year.now().getValue());

            helper.setText(htmlContent, true); // true enables HTML

            mailSender.send(message);
            log.debug("OTP email sent to {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
