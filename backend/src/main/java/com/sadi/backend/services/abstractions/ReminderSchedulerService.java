package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.requests.ReminderReq;

public interface ReminderSchedulerService {
    void scheduleReminder(ReminderReq req);
}
