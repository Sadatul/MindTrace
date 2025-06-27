package com.sadi.backend.services.impls;

import com.sadi.backend.configs.RabbitConfig;
import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.services.abstractions.ReminderSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReminderSchedulerServiceImpl implements ReminderSchedulerService {
    public static final String REDIS_REMINDER_SET_KEY = "delayed:reminders";
    public static final String REDIS_REMINDER_DETAILS_KEY = "delayed:reminders:details";
    private final RabbitTemplate rabbitTemplate;

    private static final long MAX_RABBIT_DELAY = 5 * 60 * 1000;
    private final RedisTemplate<String, Object> redisTemplate;

    public void scheduleReminder(ReminderReq req){
        Optional<Instant> nextExecution = getNextExecution(req.getCronExpression(), ZoneId.of(req.getZoneId()));
        if(nextExecution.isEmpty()){
            log.info("No next reminder found");
            return;
        }

        Instant nextRun = nextExecution.get();
        long delay = Duration.between(Instant.now(), nextRun).toMillis();

        if(delay <= MAX_RABBIT_DELAY)
            sendToRabbitMq(req, delay);
        else
            sendToRedis(req, delay);
    }

    private void sendToRedis(ReminderReq req, long delay) {
        Long redisTime = getRedisTime();
        UUID id = UUID.randomUUID();
        log.info("Next {} {}", redisTime, delay);
        redisTemplate.opsForZSet().add(REDIS_REMINDER_SET_KEY, id.toString(), redisTime + delay);
        redisTemplate.opsForHash().put(REDIS_REMINDER_DETAILS_KEY, id.toString(), req);
    }

    private void sendToRabbitMq(ReminderReq req, long delay) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.DELAYED_EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                req,
                message -> {
                    message.getMessageProperties().setDelayLong(delay);
                    return message;
                }
        );
    }

    /**
     * Get the current time from Redis server.
     * This is used to ensure that the time is consistent across all server instances
     *
     * @return Current time in milliseconds since epoch.
     */
    private Long getRedisTime() {
        return redisTemplate.execute((RedisCallback<Long>) connection -> {
            return connection.time();
        });
    }

    private Optional<Instant> getNextExecution(String cronExpression, ZoneId timezone) {
        CronExpression cron = CronExpression.parse(cronExpression);
        ZonedDateTime nextZonedTime = cron.next(ZonedDateTime.now(timezone));
        if (nextZonedTime == null)
            return Optional.empty();

        return Optional.of(nextZonedTime.toInstant());
    }
}
