package com.sadi.backend.services.impls;

import com.sadi.backend.configs.RabbitConfig;
import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.services.abstractions.ReminderSchedulerService;
import com.sadi.backend.services.abstractions.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReminderRabbitMqConsumerService {
    private final ReminderSchedulerService reminderSchedulerService;
    private final ReminderService reminderService;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void handleMessage(ReminderReq request) {

        reminderService.sendReminder(request);

        if(request.getIsRecurring()) {
            log.debug("Scheduling next reminder for recurring request: {}", request);
            reminderSchedulerService.scheduleReminder(request);
        }
    }
}
