package com.sadi.backend.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TempOnlineRequest(
        @NotNull
        @Size(min = 1, max = 255)
        String name
) {
    /**
     * We are using validations here. Name can't be null and must be between 1 and 255 characters.
     */
}
