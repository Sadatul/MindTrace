package com.sadi.backend.services.impls;

import com.sadi.backend.configs.RabbitConfig;
import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.services.abstractions.ReminderSchedulerService;
import com.sadi.backend.services.abstractions.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReminderRabbitMqConsumerService {
    private final ReminderSchedulerService reminderSchedulerService;
    private final ReminderService reminderService;
    private final RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleMessage(ReminderDTO request) {

        String key = ReminderSchedulerServiceImpl.REDIS_DELETED_REMINDERS_KEY + request.getId().toString();
        boolean exists = redisTemplate.opsForValue().get(key) != null;
        if (exists) {
            redisTemplate.delete(key);
            return;
        };

        reminderService.sendReminder(request);

        if(request.getIsRecurring()) {
            log.debug("Scheduling next reminder for recurring request: {}", request);
            reminderSchedulerService.scheduleReminder(request);
            reminderService.updateNextExecution(request.getId(), request.getCronExpression(), request.getZoneId());
        }
        else{
            reminderService.deleteReminderById(request.getId());
        }
    }
}
