package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.services.abstractions.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {
    @Override
    public void sendReminder(ReminderReq req) {
        log.info("Sending Reminder Request {}", req);
    }
}
