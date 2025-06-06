package com.sadi.backend.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

public record LogDTO(
        String details,
        LocalDate date,
        LocalTime time
) {
}
