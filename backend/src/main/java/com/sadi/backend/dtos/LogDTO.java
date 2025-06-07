package com.sadi.backend.dtos;

import io.qdrant.client.grpc.Points;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

public record LogDTO(
        String details,
        LocalDate date,
        LocalTime time
) {
    public LogDTO(Points.ScoredPoint point, ZoneId zoneId) {
        this(
                point.getPayloadMap().get("doc_content").getStringValue(), // details from document content
                extractDate(point, zoneId),
                extractTime(point, zoneId)
        );
    }

    private static LocalDate extractDate(Points.ScoredPoint point, ZoneId zoneId) {
        String createdAtStr = point.getPayloadMap().get("createdAt").getStringValue();
        return Instant.parse(createdAtStr).atZone(zoneId).toLocalDate();
    }

    private static LocalTime extractTime(Points.ScoredPoint point, ZoneId zoneId) {
        String createdAtStr = point.getPayloadMap().get("createdAt").getStringValue();
        return Instant.parse(createdAtStr).atZone(zoneId).toLocalTime();
    }
}
