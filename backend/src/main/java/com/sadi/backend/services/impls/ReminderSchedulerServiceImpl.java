package com.sadi.backend.services.impls;

import com.sadi.backend.configs.RabbitConfig;
import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.entities.Reminder;
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
    public static final String REDIS_DELETED_REMINDERS_KEY = "delayed:reminders:deleted";
    private final RabbitTemplate rabbitTemplate;

    private static final long MAX_RABBIT_DELAY = 5 * 60 * 1000;
    private static final long MAX_REDIS_DELAY = 10 * 60 * 1000; // 24 hours
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void scheduleReminder(ReminderDTO req){
        Optional<Instant> nextExecution = getNextExecution(req.getCronExpression(), ZoneId.of(req.getZoneId()));
        if(nextExecution.isEmpty()){
            log.debug("No next reminder found");
            return;
        }

        Instant nextRun = nextExecution.get();
        long delay = Duration.between(Instant.now(), nextRun).toMillis();

        if(delay <= MAX_RABBIT_DELAY)
            sendToRabbitMq(req, delay);
        else if(delay <= MAX_REDIS_DELAY)
            sendToRedis(req, delay);

        // If the delay is more than MAX_REDIS_DELAY, we do not schedule it, rather it stays in db
    }

    @Override
    public void deleteScheduledReminder(Reminder reminder){
        log.debug("Scheduled reminder being delete {}", reminder.getId());
        UUID id = reminder.getId();
        boolean exists = redisTemplate.opsForZSet().score(REDIS_REMINDER_SET_KEY, id.toString()) != null;
        if(exists){
            redisTemplate.opsForZSet().remove(REDIS_REMINDER_DETAILS_KEY, id);
            redisTemplate.opsForHash().delete(REDIS_REMINDER_SET_KEY, id);
            return;
        }

        // We are giving the double the expiration time to ensure that the reminder is not sent again
        redisTemplate.opsForValue().set(REDIS_DELETED_REMINDERS_KEY + id.toString(), "",
                Duration.ofMillis(MAX_RABBIT_DELAY * 2));
    }

    @Override
    public boolean isReminderScheduled(String cronExpression, ZoneId timezone) {
        Optional<Instant> nextExecution = getNextExecution(cronExpression, timezone);
        if (nextExecution.isEmpty()) {
            return false;
        }
        Instant nextRun = nextExecution.get();
        long delay = Duration.between(Instant.now(), nextRun).toMillis();
        return delay <= MAX_REDIS_DELAY && delay > 0;
    }

    private void sendToRedis(ReminderDTO req, long delay) {
        Long redisTime = getRedisTime();
        redisTemplate.opsForZSet().add(REDIS_REMINDER_SET_KEY, req.getId().toString(), redisTime + delay);
        redisTemplate.opsForHash().put(REDIS_REMINDER_DETAILS_KEY, req.getId().toString(), req);
    }

    private void sendToRabbitMq(ReminderDTO req, long delay) {
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

    @Override
    public Optional<Instant> getNextExecution(String cronExpression, ZoneId timezone) {
        CronExpression cron = CronExpression.parse(cronExpression);
        ZonedDateTime nextZonedTime = cron.next(ZonedDateTime.now(timezone));
        if (nextZonedTime == null)
            return Optional.empty();

        return Optional.of(nextZonedTime.toInstant());
    }
}
