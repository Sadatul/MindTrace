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
                extractDate(point.getPayloadMap().get("createdAt").getStringValue(), zoneId),
                extractTime(point.getPayloadMap().get("createdAt").getStringValue(), zoneId)
        );
    }

    public LogDTO(Points.RetrievedPoint point, ZoneId zoneId) {
        this(
                point.getPayloadMap().get("doc_content").getStringValue(), // details from document content
                extractDate(point.getPayloadMap().get("createdAt").getStringValue(), zoneId),
                extractTime(point.getPayloadMap().get("createdAt").getStringValue(), zoneId)
        );
    }

    private static LocalDate extractDate(String createdAtStr, ZoneId zoneId) {
        return Instant.parse(createdAtStr).atZone(zoneId).toLocalDate();
    }

    private static LocalTime extractTime(String createdAtStr, ZoneId zoneId) {
        return Instant.parse(createdAtStr).atZone(zoneId).toLocalTime();
    }
}
