package com.sadi.backend.services.impls;

import com.sadi.backend.entities.User;
import com.sadi.backend.repositories.UserRepository;
import com.sadi.backend.services.abstractions.EmailService;
import com.sadi.backend.services.abstractions.UserVerificationService;
import com.sadi.backend.utils.CodeGenerator;
import com.sadi.backend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
public class UserVerificationServiceRedis implements UserVerificationService {
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final String REDIS_OTP_KEY_PREFIX = "otp:patient_registration:";


    @Override
    public String cacheOtp() {
        String id = SecurityUtils.getName();
        User user = userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
        String otp = CodeGenerator.generateOtp();
        emailService.sendOtpEmail(user.getEmail(), otp);
        redisTemplate.opsForValue().set(REDIS_OTP_KEY_PREFIX + id, otp, Duration.ofMinutes(10));
        return otp;
    }

    @Override
    public Boolean verifyOtp(String userId, String otp) {
        String storedOtp = redisTemplate.opsForValue().get(REDIS_OTP_KEY_PREFIX + userId);
        boolean result = storedOtp != null && storedOtp.equals(otp);
        if(result)
            redisTemplate.delete(REDIS_OTP_KEY_PREFIX + userId);
        return result;
    }
}
