package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.entities.Reminder;
import com.sadi.backend.enums.ReminderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface ReminderService {
    void sendReminder(ReminderDTO req);
    UUID createReminder(String userId, ReminderReq req);
    void deleteReminder(String userId, UUID id, boolean isOwner);

    void updateNextExecution(UUID id, @NotNull String cronExpression, String zoneId);
    void deleteReminderById(UUID id);
    Page<Reminder> getReminders(String userId, ReminderType type, Instant start, Instant end, Pageable pageable);
    Reminder getReminder(UUID id);
}
