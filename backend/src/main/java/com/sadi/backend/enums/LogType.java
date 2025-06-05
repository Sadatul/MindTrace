package com.sadi.backend.enums;

import java.util.Arrays;
import java.util.Optional;

public enum LogType {
    EATING,
    MEDICINE,
    SOCIAL,
    OUTINGS,
    BATHING,
    START;

    /**
     * Safely parses a string into a LogType enum.
     * Returns Optional.empty() if no match is found.
     */
    public static Optional<LogType> fromString(String value) {
        if (value == null) return Optional.empty();
        return Arrays.stream(LogType.values())
                .filter(type -> type.name().equalsIgnoreCase(value))
                .findFirst();
    }
}
