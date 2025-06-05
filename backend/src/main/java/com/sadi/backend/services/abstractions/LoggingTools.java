package com.sadi.backend.services.abstractions;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;

public interface LoggingTools {
    String saveLog(String logType, String details, Integer minutes, ToolContext toolContext);
}
