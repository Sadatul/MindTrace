package com.sadi.backend.unittests;

import com.sadi.backend.entities.User;
import com.sadi.backend.repositories.UserRepository;
import com.sadi.backend.services.abstractions.EmailService;
import com.sadi.backend.services.impls.UserVerificationServiceRedis;
import com.sadi.backend.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserVerificationServiceRedisTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UserVerificationServiceRedis verificationService;


    @Test
    void testCacheOtp_successful() {
        String userId = "user-123";
        String email = "test@example.com";
        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getName).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            String otp = verificationService.cacheOtp();

            assertNotNull(otp);
            verify(emailService).sendOtpEmail(email, otp);
            verify(valueOperations).set("otp:patient_registration:" + userId, otp, Duration.ofMinutes(10));
        }
    }

    @Test
    void testCacheOtp_userNotFound() {
        String userId = "user-456";
        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getName).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> {
                verificationService.cacheOtp();
            });

            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Test
    void testVerifyOtp_correctOtp() {
        String userId = "user-789";
        String otp = "123456";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get("otp:patient_registration:" + userId)).thenReturn(otp);

        Boolean result = verificationService.verifyOtp(userId, otp);

        assertTrue(result);
        verify(redisTemplate).delete("otp:patient_registration:" + userId);
    }

    @Test
    void testVerifyOtp_incorrectOtp() {
        String userId = "user-789";
        String otp = "123456";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get("otp:patient_registration:" + userId)).thenReturn("999999");

        Boolean result = verificationService.verifyOtp(userId, otp);

        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testVerifyOtp_nullOtp() {
        String userId = "user-789";
        String otp = "123456";

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForValue().get("otp:patient_registration:" + userId)).thenReturn(null);

        Boolean result = verificationService.verifyOtp(userId, otp);

        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }
}
