package com.sadi.backend.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.services.impls.ReminderSchedulerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisScheduler {
    private static final int SCHEDULER_INTERVAL_MS = 5 * 60 * 1000; // 5 minutes
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

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
                ReminderReq request = objectMapper.convertValue(objReq, ReminderReq.class);
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
    private Long getRedisTime() {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.time();
        });
    }
}
