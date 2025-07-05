package com.sadi.backend.dtos.responses;

import com.sadi.backend.entities.Reminder;
import com.sadi.backend.enums.ReminderType;

import java.time.Instant;
import java.util.UUID;

public record ReminderFullRes(
        UUID id,
        ReminderType reminderType,
        String title,
        String description,
        String cronExpression,
        Instant createdAt,
        Boolean isRecurring,
        Instant nextExecution,
        String zoneId
) {
    public static ReminderFullRes getReminderFullResFromReminder(Reminder reminder){
        return new ReminderFullRes(
                reminder.getId(),
                reminder.getType(),
                reminder.getTitle(),
                reminder.getDescription(),
                reminder.getCronExpression(),
                reminder.getCreatedAt(),
                reminder.getIsRecurring(),
                Instant.ofEpochMilli(reminder.getNextExecution()),
                reminder.getZoneId()
        );
    }
}
