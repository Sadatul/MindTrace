package com.sadi.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.repositories.ReminderRepository;
import com.sadi.backend.services.abstractions.ReminderSchedulerService;
import com.sadi.backend.services.impls.ReminderSchedulerServiceImpl;
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
    private static final int SCHEDULER_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes
    private static final long DB_SCHEDULER_INTERVAL_MS = 10 * 60 * 1000; // 24 hours
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final ReminderRepository reminderRepository;
    private final ReminderSchedulerService reminderSchedulerService;

    @Scheduled(fixedRate = SCHEDULER_INTERVAL_MS)
    public void processDueNotifications() {
        long now = getRedisTime();
        long upperBound = now + SCHEDULER_INTERVAL_MS;

        Set<ZSetOperations.TypedTuple<Object>> dueNotifications = redisTemplate.opsForZSet()
            .rangeByScoreWithScores(ReminderSchedulerServiceImpl.REDIS_REMINDER_SET_KEY, 0, upperBound);

        assert dueNotifications != null;
        for (ZSetOperations.TypedTuple<Object> tuple : dueNotifications) {
            String notificationId = (String) tuple.getValue();
            Double score = tuple.getScore();
            assert score != null;
            long scheduledTime = score.longValue();
            long remainingDelay = scheduledTime - now;

            redisTemplate.opsForZSet().remove(ReminderSchedulerServiceImpl.REDIS_REMINDER_SET_KEY, notificationId);
            assert notificationId != null;
            Object objReq = redisTemplate.opsForHash()
                    .get(ReminderSchedulerServiceImpl.REDIS_REMINDER_DETAILS_KEY, notificationId
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

                redisTemplate.opsForHash().delete(ReminderSchedulerServiceImpl.REDIS_REMINDER_DETAILS_KEY, notificationId);
            }
        }
    }

    @Scheduled(fixedRate = DB_SCHEDULER_INTERVAL_MS)
    public void processDatabaseReminders() {
        log.debug("Processing DB Reminders");
        // Subtract 1 second to ensure we catch reminders that might be missed to scheduling delay
        long now = Instant.now().toEpochMilli() - 1000;
        List<UUID> ids = new ArrayList<>();
        reminderRepository.findReminderByNextExecutionBetweenAndIsScheduled(now, now + DB_SCHEDULER_INTERVAL_MS, false)
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
