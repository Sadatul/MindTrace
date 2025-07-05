package com.sadi.backend.unittests;

import com.sadi.backend.entities.User;
import com.sadi.backend.services.UserService;
import com.sadi.backend.services.impls.TelegramServiceRedisImpl;
import com.sadi.backend.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TelegramServiceRedisImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private UserService userService;

    @InjectMocks
    private TelegramServiceRedisImpl telegramService;

    @BeforeEach
    void setUp() {
        // Manually inject botToken since @Value doesn't work in unit tests
        ReflectionTestUtils.setField(telegramService, "botToken", "dummy-bot-token");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGetUUIDForRegistration_storesUserIdInRedis() {
        String userId = "user-123";
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getName).thenReturn(userId);

            String uuidStr = telegramService.getUUIDForRegistration();

            verify(valueOperations).set(
                    eq("telegram:registration:" + UUID.fromString(uuidStr)),
                    eq(userId),
                    eq(Duration.ofSeconds(600))
            );

            assertNotNull(uuidStr);
        }
    }

    @Test
    void testRegisterUser_successful() {
        String chatId = "123456789";
        String uuid = UUID.randomUUID().toString();
        String redisKey = "telegram:registration:" + uuid;
        String userId = "user-123";
        User user = new User(userId);

        when(redisTemplate.opsForValue().get(redisKey)).thenReturn(userId);
        when(userService.getUser(userId)).thenReturn(user);

        telegramService.registerUser(chatId, uuid);

        assertEquals(chatId, user.getTelegramChatId());
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    void testRegisterUser_expiredUUID() {
        String uuid = UUID.randomUUID().toString();
        String redisKey = "telegram:registration:" + uuid;

        when(redisTemplate.opsForValue().get(redisKey)).thenReturn(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                telegramService.registerUser("98765", uuid)
        );

        assertEquals(HttpStatus.REQUEST_TIMEOUT, ex.getStatusCode());
    }
}
