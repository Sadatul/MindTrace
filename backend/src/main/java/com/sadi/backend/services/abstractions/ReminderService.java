package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.requests.ReminderReq;

public interface ReminderService {
    void sendReminder(ReminderReq req);
}
