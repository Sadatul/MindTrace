package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.entities.Log;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.LogType;
import com.sadi.backend.services.abstractions.LogService;
import com.sadi.backend.services.abstractions.LoggingTools;
import com.sadi.backend.utils.BasicUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class LoggingToolsImpl implements LoggingTools {
    private final LogService logService;

    public LoggingToolsImpl(LogService logService) {
        this.logService = logService;
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
        return "Log Saved Successfully";
    }

    @Override
    @Tool(description = """
            Use this tool to retrieve logs for summarization within the specified start and end timestamps. Remember start and end must be in iso time format.
            With each log you will get a date specifying the date on which the log was created and time on which the log was created
            """)
    public List<LogDTO> getLogs(
            @ToolParam(description = "Start time in iso format")
            String startDatetime,
            @ToolParam(description = "End time in iso format")
            String endDatetime,
            ToolContext toolContext
    ) {
        String userId = (String) toolContext.getContext().get("userId");
        String zoneId = (String) toolContext.getContext().get("zone");

        Instant start =  BasicUtils.getInstantISOStringAndZoneId(startDatetime, zoneId);
        Instant end =  BasicUtils.getInstantISOStringAndZoneId(endDatetime, zoneId);

        log.debug("Get logs between {} and {}, {}, {}", start, end, userId, zoneId);
        return logService.getLogsByTimeStamp(userId, zoneId, start, end);
    }

    @Override
    @Tool(description = """
            Use these tool to retrieve logs related to certain query. This tool allows you to filter events based on timestamp & log type.
            """)
    public List<LogDTO> getLogsForSpecificTopic(
            @ToolParam(description = "We will perform a similarity search based on this queryString")
            String queryString,
            @ToolParam(description = "Pass the type of log. Must be one of the following values (EATING, MEDICINE, SOCIAL, OUTINGS, BATHING)")
            String logType,
            @ToolParam(description = "Start time in iso format. Provide only if you want to filter based on timestamp", required = false)
            String startDateTime,
            @ToolParam(description = "End time in iso format. Provide only if you want to filter based on timestamp", required = false)
            String endDateTime,
            ToolContext toolContext
    ) {
        String userId = (String) toolContext.getContext().get("userId");
        String zoneId = (String) toolContext.getContext().get("zone");

        Instant start = null;
        if(startDateTime != null){
            start = BasicUtils.getInstantISOStringAndZoneId(startDateTime, zoneId);
        }
        Instant end = null;
        if(endDateTime != null){
            end = BasicUtils.getInstantISOStringAndZoneId(endDateTime, zoneId);
        }

        log.debug("Get logs for specific topic with query: {}, logType: {}, userId: {}, start: {}, end: {}", queryString, logType, userId, start, end);
        return logService.getLogsByQuery(userId, zoneId, queryString, logType, start, end);
    }
}
