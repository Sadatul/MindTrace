package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.services.abstractions.ReminderSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
@Slf4j
public class ReminderSenderServiceTestImpl implements ReminderSenderService {
    @Override
    public void sendReminder(ReminderDTO req) {
        log.info("Sending Reminder Request {}", req);
    }
}
