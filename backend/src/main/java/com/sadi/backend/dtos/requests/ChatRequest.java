package com.sadi.backend.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotNull(message = "Must provide a query")
        @Size(min = 1, max = 2000, message = "Query must be between 1 and 2000 characters")
        String query,

        String zone
) {
        public ChatRequest {
                // Compact constructor - applies defaults
                if (zone == null || zone.trim().isEmpty()) {
                        zone = "Asia/Dhaka";
                }
        }
}
