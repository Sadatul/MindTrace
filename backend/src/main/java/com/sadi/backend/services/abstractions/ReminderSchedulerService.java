package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.entities.Reminder;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

public interface ReminderSchedulerService {
    void scheduleReminder(ReminderDTO req);
    Optional<Instant> getNextExecution(String cronExpression, ZoneId timezone);
    void deleteScheduledReminder(Reminder reminder);
    boolean isReminderScheduled(String cronExpression, ZoneId timezone);
}
