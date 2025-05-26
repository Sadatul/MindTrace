package com.sadi.backend.services.abstractions;

public interface EmailService {
    void sendSimpleEmail(String to, String subject, String text);
    void sendOtpEmail(String to, String otp);
}
