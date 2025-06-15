package com.sadi.backend.dtos.requests;

import com.sadi.backend.enums.LogType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateLogRequest(
        @NotNull(message = "Type cannot be null")
        LogType type,

        @NotNull(message = "Description cannot be null")
        @Size(min = 1, max = 65535, message = "Description must be between 1 and 65535 characters")
        String description,

        @NotNull(message = "time cannot be null")
        Long time
) {
}
