package com.sadi.backend.services.impls;

import com.sadi.backend.services.abstractions.LoggingTools;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingToolsImpl implements LoggingTools {
    @Tool(description = "User this tool to saveLogs")
    public String saveLog(
            @ToolParam(description = "Pass the type of log. Must be one of the following values (EATING, MEDICINE, SOCIAL, OUTINGS, BATHING, START)")
            String logType,
            @ToolParam(description = "The details of the log; Don't include timing details here")
            String details,
            @ToolParam(description = "Give an Integer value of how long ago (in minutes) the event took place")
            Integer minutes,
            ToolContext toolContext
    )
    {
        String userId = (String) toolContext.getContext().get("userId");
//        System.out.println(logType + " " + details + " " + userId + " " + minutes);
        log.info("{} {} {}", logType, userId, details);
        return "Log Saved Successfully";
    }
}
