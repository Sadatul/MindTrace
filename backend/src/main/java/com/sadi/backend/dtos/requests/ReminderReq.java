package com.sadi.backend.dtos.requests;

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
public class ReminderReq{
        @NotNull
        String userId;

        @NotNull
        @Size(min =  1, max = 128, message = "Title must be between 1 and 128 characters")
        String title;

        @NotNull
        @Size(min = 1, max = 512, message = "Description must be between 1 and 512 characters")
        String description;

        @NotNull
        @ValidCron
        String cronExpression;

        @ValidZoneId
        String zoneId;

        Boolean isRecurring;

        public ReminderReq() {
                isRecurring = false;
                zoneId = "Asia/Dhaka";
        }
}
