package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.requests.ReminderDTO;

public interface ReminderSenderService {
    void sendReminder(ReminderDTO req);
}
