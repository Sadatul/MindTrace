package com.sadi.backend.services.impls;

import com.sadi.backend.entities.User;
import com.sadi.backend.services.UserService;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelegramServiceRedisImpl {

    @Value("${BOT_TOKEN}")
    private String botToken;

    private final String TELEGRAM_KEY_PREFIX = "telegram:registration:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;

    public String getUUIDForRegistration() {
        String userId = SecurityUtils.getName();
        UUID uuid = UUID.randomUUID();
        String key = TELEGRAM_KEY_PREFIX + uuid;
        redisTemplate.opsForValue().set(key, userId, Duration.of(600, ChronoUnit.SECONDS));
        return  uuid.toString();
    }

    @Transactional
    public void registerUser(@NotNull(message = "Chat ID cannot be null")
                             @Size(min = 1, max = 255, message = "Chat ID must be between 1 and 255 characters") String chatId,
                             @NotNull(message = "UUID cannot be null") String uuid) {

        String key = TELEGRAM_KEY_PREFIX + uuid;
        Object obj = redisTemplate.opsForValue().get(key);
        if(obj == null) {
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT,
                    "The UUID is invalid or has expired. Please request a new UUID.");
        }
        String userId = (String) obj;
        User user = userService.getUser(userId);
        user.setTelegramChatId(chatId);
        redisTemplate.delete(key);
    }

    public void sendMessage(String chatId, String message) {
        log.info("Sending message to chat {}: {}", chatId, message);

        String apiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        RestClient restClient = RestClient.builder().build();

        Map<String, Object> payload = Map.of(
                "chat_id", chatId,
                "text", message
        );

        try {
            restClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
