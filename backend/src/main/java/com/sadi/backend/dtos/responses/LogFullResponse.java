package com.sadi.backend.dtos.responses;

import com.sadi.backend.enums.LogType;

import java.time.Instant;
import java.util.UUID;

public record LogFullResponse
        (UUID id, LogType type, String description, Instant createdAt) {
}
