package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.LogDTO;
import org.springframework.ai.chat.model.ToolContext;

import java.time.Instant;
import java.util.List;

public interface LoggingTools {
    String saveLog(String logType, String details, Integer minutes, ToolContext toolContext);
    List<LogDTO> getLogs(Instant start, Instant end, ToolContext toolContext);
}
