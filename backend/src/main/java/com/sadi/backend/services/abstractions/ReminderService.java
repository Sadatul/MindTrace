package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.dtos.requests.ReminderReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public interface ReminderService {
    void sendReminder(ReminderDTO req);
    UUID createReminder(String userId, ReminderReq req);
    void deleteReminder(String userId, UUID id, boolean isOwner);

    void updateNextExecution(UUID id, @NotNull String cronExpression, String zoneId);
    void deleteReminderById(UUID id);
}
