package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.entities.Log;

import java.time.Instant;
import java.util.List;

public interface LogService {
    void saveLog(Log log);
    List<LogDTO> getLogsByTimeStamp(String userId, String zoneId, Instant start, Instant end);
}
