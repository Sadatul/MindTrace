package com.sadi.backend.dtos.requests;

import com.sadi.backend.enums.ReminderType;
import com.sadi.backend.utils.ValidCron;
import com.sadi.backend.utils.ValidZoneId;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ReminderReq {
    @NotNull
    @Size(min =  1, max = 128, message = "Title must be between 1 and 128 characters")
    private String title;

    @NotNull
    @Size(min = 1, max = 512, message = "Description must be between 1 and 512 characters")
    private String description;

    @NotNull(message = "Reminder type cannot be null")
    private ReminderType reminderType;

    @NotNull
    @ValidCron
    private String cronExpression;

    @ValidZoneId
    private String zoneId;

    Boolean isRecurring;

    public ReminderReq() {
        isRecurring = false;
        zoneId = "Asia/Dhaka";
    }
}
