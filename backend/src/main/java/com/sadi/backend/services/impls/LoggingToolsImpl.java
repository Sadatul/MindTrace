package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.entities.Log;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.LogType;
import com.sadi.backend.services.abstractions.LogService;
import com.sadi.backend.services.abstractions.LoggingTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class LoggingToolsImpl implements LoggingTools {
    private final LogService logService;
    private final VectorStore logVectorStore;

    public LoggingToolsImpl(LogService logService, VectorStore logVectorStore) {
        this.logService = logService;
        this.logVectorStore = logVectorStore;
    }

    @Tool(description = "User this tool to saveLogs")
    public String saveLog(
            @ToolParam(description = "Pass the type of log. Must be one of the following values (EATING, MEDICINE, SOCIAL, OUTINGS, BATHING)")
            String logType,
            @ToolParam(description = "The details of the log; Don't include timing details here")
            String details,
            @ToolParam(description = "Give an Integer value of how long ago (in minutes) the event took place")
            Integer minutes,
            ToolContext toolContext
    )
    {
        String userId = (String) toolContext.getContext().get("userId");
        User user = new User(userId);
        Optional<LogType> type = LogType.fromString(logType);
        if(type.isEmpty()){
            return "failed to save log due to invalid log type";
        }
        log.debug("Saving log {} to {}", logType, userId);
        Log myLog = new Log(user, type.get(), details, Instant.now().minus(minutes, ChronoUnit.MINUTES));
        logService.saveLog(myLog);
        logVectorStore.add(List.of(new Document(details, myLog.getMetadata())));
        return "Log Saved Successfully";
    }

    @Override
    @Tool(description = """
            Use this tool to get logs between start and end timestamp. Remember start and end must be in utc time format.
            With each log you will get a date specifying the date on which the log was created and time on which the log was created
            """)
    public List<LogDTO> getLogs(
            @ToolParam(description = "Start time in utc format")
            Instant start,
            @ToolParam(description = "End time in utc format")
            Instant end,
            ToolContext toolContext
    ) {
        String userId = (String) toolContext.getContext().get("userId");
        String zoneId = (String) toolContext.getContext().get("zone");

        log.debug("Get logs between {} and {}, {}, {}", start, end, userId, zoneId);
        return logService.getLogsByTimeStamp(userId, zoneId, start, end);
    }
}
