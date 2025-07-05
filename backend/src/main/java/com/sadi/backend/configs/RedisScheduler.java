package com.sadi.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.repositories.ReminderRepository;
import com.sadi.backend.services.abstractions.ReminderSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisScheduler {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ReminderRepository reminderRepository;
    private final ReminderSchedulerService reminderSchedulerService;
    private final ReminderSchedulerConfig reminderSchedulerConfig;

    @Scheduled(fixedRateString = "${reminder.rabbit.max-delay}")
    public void processDueNotifications() {
        long now = getRedisTime();
        long upperBound = now + reminderSchedulerConfig.getRabbit().getMaxDelay();

        Set<ZSetOperations.TypedTuple<Object>> dueNotifications = redisTemplate.opsForZSet()
            .rangeByScoreWithScores(reminderSchedulerConfig.getRedis().getReminderSetKey(), 0, upperBound);

        assert dueNotifications != null;
        for (ZSetOperations.TypedTuple<Object> tuple : dueNotifications) {
            String notificationId = (String) tuple.getValue();
            Double score = tuple.getScore();
            assert score != null;
            long scheduledTime = score.longValue();
            long remainingDelay = scheduledTime - now;

            redisTemplate.opsForZSet().remove(reminderSchedulerConfig.getRedis().getReminderSetKey(), notificationId);
            assert notificationId != null;
            Object objReq = redisTemplate.opsForHash()
                    .get(reminderSchedulerConfig.getRedis().getReminderDetailsKey(), notificationId
                    );

            if (objReq != null) {
                // Send to RabbitMQ with precise remaining delay
                ReminderDTO request = objectMapper.convertValue(objReq, ReminderDTO.class);
                rabbitTemplate.convertAndSend(
                        RabbitConfig.DELAYED_EXCHANGE,
                        RabbitConfig.ROUTING_KEY,
                        request,
                        message -> {
                            message.getMessageProperties().setDelayLong(Math.max(0, remainingDelay));
                            return message;
                        }
                );

                redisTemplate.opsForHash().delete(reminderSchedulerConfig.getRedis().getReminderDetailsKey(), notificationId);
            }
        }
    }

    @Scheduled(fixedRateString = "${reminder.redis.max-delay}")
    public void processDatabaseReminders() {
        log.debug("Processing DB Reminders");
        // Subtract 1 second to ensure we catch reminders that might be missed to scheduling delay
        long now = Instant.now().toEpochMilli() - 1000;
        List<UUID> ids = new ArrayList<>();
        reminderRepository.findReminderByNextExecutionBetweenAndIsScheduled(now, now + reminderSchedulerConfig.getRedis().getMaxDelay(), false)
                .forEach(reminder -> {
                    ReminderDTO dto = new ReminderDTO(reminder);
                    reminderSchedulerService.scheduleReminder(dto);
                    ids.add(reminder.getId());
                });
        if(!ids.isEmpty())
            reminderRepository.markScheduled(ids);
    }
    private Long getRedisTime() {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.time();
        });
    }
}
