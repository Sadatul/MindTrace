package com.sadi.backend.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BasicUtils {
    public static Instant getInstantISOStringAndZoneId(String isoString, String zoneId) {
        LocalDateTime localDateTime = LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return localDateTime.atZone(ZoneId.of(zoneId)).toInstant();
    }

    public static  String getISOStringFromZoneIdAndInstant(String zoneId, Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.of(zoneId))
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
}
