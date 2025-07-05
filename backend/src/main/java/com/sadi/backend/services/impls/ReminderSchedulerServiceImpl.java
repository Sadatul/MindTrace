package com.sadi.backend.services.impls;

import com.sadi.backend.configs.RabbitConfig;
import com.sadi.backend.configs.ReminderSchedulerConfig;
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
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ReminderSchedulerConfig reminderSchedulerConfig;

    @Override
    public void scheduleReminder(ReminderDTO req){
        Optional<Integer> optionalDelay = getDelay(req.getCronExpression(), ZoneId.of(req.getZoneId()));
        if(optionalDelay.isEmpty()){
            log.debug("No next reminder found");
            return;
        }

        long delay = optionalDelay.get();
        if(delay <= reminderSchedulerConfig.getRabbit().getMaxDelay())
            sendToRabbitMq(req, delay);
        else if(delay <= reminderSchedulerConfig.getRedis().getMaxDelay())
            sendToRedis(req, delay);

        // If the delay is more than MAX_REDIS_DELAY, we do not schedule it, rather it stays in db
    }

    @Override
    public void deleteScheduledReminder(Reminder reminder){
        log.debug("Scheduled reminder being delete {}", reminder.getId());

        if(!reminder.getIsScheduled()) return;

        UUID id = reminder.getId();
        boolean exists = redisTemplate.opsForZSet().score(reminderSchedulerConfig.getRedis().getReminderSetKey(), id.toString()) != null;
        if(exists){
            redisTemplate.opsForZSet().remove(reminderSchedulerConfig.getRedis().getReminderSetKey(), id.toString());
            redisTemplate.opsForHash().delete(reminderSchedulerConfig.getRedis().getReminderDetailsKey(), id.toString());
            return;
        }

        // We are giving the double the expiration time to ensure that the reminder is not sent again
        redisTemplate.opsForValue().set(reminderSchedulerConfig.getRedis().getDeletedRemindersKey() + id.toString(), "",
                Duration.ofMillis(reminderSchedulerConfig.getRabbit().getMaxDelay() * 2));
    }

    @Override
    public boolean isReminderScheduled(String cronExpression, ZoneId timezone) {
        Optional<Integer> delayOptional = getDelay(cronExpression, timezone);
        if (delayOptional.isEmpty()) {
            return false;
        }
        long delay = delayOptional.get();
        return delay <= reminderSchedulerConfig.getRedis().getMaxDelay() && delay >= 0;
    }

    public Optional<Integer> getDelay(String cronExpression, ZoneId timezone) {
        Instant now = Instant.now();
        Optional<Instant> nextExecution = getNextExecution(cronExpression, timezone);
        if (nextExecution.isEmpty()) {
            return Optional.empty();
        }
        Instant nextRun = nextExecution.get();
        long delay = Duration.between(now, nextRun).toMillis();
        return delay > 0 ? Optional.of((int) delay) : Optional.of(0);
    }

    private void sendToRedis(ReminderDTO req, long delay) {
        Long redisTime = getRedisTime();
        redisTemplate.opsForZSet().add(reminderSchedulerConfig.getRedis().getReminderSetKey(), req.getId().toString(), redisTime + delay);
        redisTemplate.opsForHash().put(reminderSchedulerConfig.getRedis().getReminderDetailsKey(), req.getId().toString(), req);
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
