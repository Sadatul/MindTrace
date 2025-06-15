package com.sadi.backend.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TelegramRegistrationRequest(
        @NotNull(message = "UUID cannot be null")
        String uuid,
        @NotNull(message = "Chat ID cannot be null")
        @Size(min = 1, max = 255, message = "Chat ID must be between 1 and 255 characters")
        String chatId
) {
}
