package com.sadi.backend.services.impls;

import com.sadi.backend.entities.User;
import com.sadi.backend.repositories.UserRepository;
import com.sadi.backend.services.abstractions.EmailService;
import com.sadi.backend.services.abstractions.UserVerificationService;
import com.sadi.backend.utils.CodeGenerator;
import com.sadi.backend.utils.SecurityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "none")
public class UserVerificationServiceInMemory implements UserVerificationService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    Map<String, String> otps = new HashMap<>();

    public UserVerificationServiceInMemory(EmailService emailService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    @Override
    public String cacheOtp() {
        String id = SecurityUtils.getName();
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
        String otp = CodeGenerator.generateOtp();
        emailService.sendOtpEmail(user.getEmail(), otp);
        otps.put(id, otp);
        return otp;
    }

    @Override
    public Boolean verifyOtp(String userId, String otp) {
        return otps.containsKey(userId) && otps.get(userId).equals(otp);
    }
}
