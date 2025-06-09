package com.sadi.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadi.backend.dtos.StoredMessageDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisChatMemoryRepository implements ChatMemoryRepository {
    private static final String CONVERSATION_KEY_PREFIX = "chat:conversation:";
    private static final String CONVERSATION_SET_KEY = "chat:conversations";
    private static final int EXPIRES_AFTER_CONVERSATION = 30;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisChatMemoryRepository(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public List<String> findConversationIds() {
        Set<Object> conversationIds = redisTemplate.opsForSet().members(CONVERSATION_SET_KEY);
        if (conversationIds == null || conversationIds.isEmpty()) {
            return List.of();
        }

        return conversationIds.stream()
                .map(Object::toString)
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public List<Message> findByConversationId(@NonNull String conversationId) {
        String key = CONVERSATION_KEY_PREFIX + conversationId;

        Object value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return List.of();
        }

        List<StoredMessageDTO> dtos;

        try {
            if (value instanceof List<?> list) {
                dtos = list.stream()
                        .map(obj -> objectMapper.convertValue(obj, StoredMessageDTO.class))
                        .filter(Objects::nonNull)
                        .toList();
            } else {
                dtos = objectMapper.convertValue(value,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, StoredMessageDTO.class));
            }
        } catch (Exception e) {
            System.err.println("Failed to deserialize message list: " + e.getMessage());
            return List.of();
        }

        return dtos.stream()
                .map(StoredMessageDTO::toMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(@NonNull String conversationId, List<Message> messages) {
        log.info("Saving conversation {} messages", conversationId);
        if (messages.isEmpty()) {
            return;
        }

        String key = CONVERSATION_KEY_PREFIX + conversationId;

        // Add conversation ID to the set of all conversations
        redisTemplate.opsForSet().add(CONVERSATION_SET_KEY, conversationId);

        List<StoredMessageDTO> dtos = messages.stream().map(StoredMessageDTO::new).toList();

        // Save the entire list as a single value
        redisTemplate.opsForValue().set(key, dtos, Duration.ofDays(EXPIRES_AFTER_CONVERSATION));
    }


    @Override
    public void deleteByConversationId(@NonNull String conversationId) {
        log.info("Deleting conversation {}", conversationId);
        String key = CONVERSATION_KEY_PREFIX + conversationId;

        // Delete the conversation messages
        redisTemplate.delete(key);

        // Remove conversation ID from the set
        redisTemplate.opsForSet().remove(CONVERSATION_SET_KEY, conversationId);

    }


    private StoredMessageDTO deserializeMessage(Object obj) {
        try {
            if (obj instanceof StoredMessageDTO) {
                return (StoredMessageDTO) obj;
            } else if (obj instanceof LinkedHashMap<?, ?> map) {
                // Handle deserialization from Redis
                return objectMapper.convertValue(map, StoredMessageDTO.class);
            } else {
                // Fallback: try direct conversion
                return objectMapper.convertValue(obj, StoredMessageDTO.class);
            }
        } catch (Exception e) {
            // Log error and return null to filter out corrupted messages
            System.err.println("Failed to deserialize message: " + e.getMessage());
            return null;
        }
    }
}
