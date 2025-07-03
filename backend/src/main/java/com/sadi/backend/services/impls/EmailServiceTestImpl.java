package com.sadi.backend.services.impls;

import com.sadi.backend.services.abstractions.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class EmailServiceTestImpl implements EmailService {
    @Override
    public void sendSimpleEmail(String to, String subject, String text) {

    }

    @Override
    public void sendOtpEmail(String to, String otp) {

    }
}
