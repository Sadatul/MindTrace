package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.dtos.requests.UpdateLogRequest;
import com.sadi.backend.entities.Log;
import com.sadi.backend.enums.LogType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LogService {
    UUID saveLog(Log log);
    UUID saveLog(String userId, LogType type, String description, Instant createdAt);
    List<LogDTO> getLogsByTimeStamp(String userId, String zoneId, Instant start, Instant end);
    List<LogDTO> getLogsByQuery(String userId, String zoneId, String queryString, String logType, Instant start, Instant end);
    void deleteLog(UUID id);
    void verifyOwner(Log log, String userId);
    Log getLog(UUID id);
    void updateLog(UUID id, UpdateLogRequest req);
    Page<Log> getLogs(String userId, LogType type, Instant start, Instant end, Pageable pageable);
}
