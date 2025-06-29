package com.sadi.backend.unittests;

import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.entities.Log;
import com.sadi.backend.enums.LogType;
import com.sadi.backend.services.abstractions.LogService;
import com.sadi.backend.services.impls.LoggingToolsImpl;
import com.sadi.backend.utils.BasicUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ToolContext;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoggingToolsImplTest {

    @Mock
    private LogService logService;

    @InjectMocks
    private LoggingToolsImpl loggingTools;

    private ToolContext mockToolContext(String userId, String zoneId) {
        Map<String, Object> context = new HashMap<>();
        context.put("userId", userId);
        context.put("zone", zoneId);
        return new ToolContext(context);
    }

    @Test
    void testSaveLog_successful() {
        ToolContext context = mockToolContext("user-123", "Asia/Dhaka");
        String result = loggingTools.saveLog("EATING", "Had rice and vegetables", 30, context);

        ArgumentCaptor<Log> logCaptor = ArgumentCaptor.forClass(Log.class);
        verify(logService).saveLog(logCaptor.capture());

        Log savedLog = logCaptor.getValue();
        assertEquals(LogType.EATING, savedLog.getType());
        assertEquals("Had rice and vegetables", savedLog.getDescription());
        assertEquals("user-123", savedLog.getUser().getId());
        assertTrue(savedLog.getCreatedAt().isBefore(Instant.now()));
        assertEquals("Log Saved Successfully", result);
    }

    @Test
    void testSaveLog_invalidType() {
        ToolContext context = mockToolContext("user-123", "Asia/Dhaka");
        String result = loggingTools.saveLog("INVALID_TYPE", "Some detail", 10, context);

        verify(logService, never()).saveLog(any());
        assertEquals("failed to save log due to invalid log type", result);
    }

    @Test
    void testGetLogs_callsLogServiceCorrectly() {
        ToolContext context = mockToolContext("user-abc", "UTC");
        Instant now = Instant.now();
        Instant later = now.plusSeconds(3600);

        when(logService.getLogsByTimeStamp(anyString(), anyString(), any(), any()))
                .thenReturn(List.of(new LogDTO("Test", now.atZone(ZoneId.of("UTC")).toLocalDate(), now.atZone(ZoneId.of("UTC")).toLocalTime())));

        List<LogDTO> logs = loggingTools.getLogs(BasicUtils.getISOStringFromZoneIdAndInstant("UTC", now),
                BasicUtils.getISOStringFromZoneIdAndInstant("UTC", later), context);

        assertEquals(1, logs.size());
        verify(logService).getLogsByTimeStamp("user-abc", "UTC", now, later);
    }

    @Test
    void testGetLogsForSpecificTopic_withTimestamps() {
        ToolContext context = mockToolContext("user-def", "UTC");
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();

        when(logService.getLogsByQuery(anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(List.of(new LogDTO("Details", start.atZone(ZoneId.of("UTC")).toLocalDate(), start.atZone(ZoneId.of("UTC")).toLocalTime())));

        List<LogDTO> result = loggingTools.getLogsForSpecificTopic("medicine", "MEDICINE",
                BasicUtils.getISOStringFromZoneIdAndInstant("UTC", start), BasicUtils.getISOStringFromZoneIdAndInstant("UTC", end), context);

        assertEquals(1, result.size());
        verify(logService).getLogsByQuery("user-def", "UTC", "medicine", "MEDICINE", start, end);
    }

    @Test
    void testGetLogsForSpecificTopic_withoutTimestamps() {
        ToolContext context = mockToolContext("user-def", "UTC");

        when(logService.getLogsByQuery(anyString(), anyString(), anyString(), anyString(), isNull(), isNull()))
                .thenReturn(List.of(new LogDTO("Details", LocalDate.now(), LocalTime.now())));

        List<LogDTO> result = loggingTools.getLogsForSpecificTopic("social", "SOCIAL", null, null, context);

        assertEquals(1, result.size());
        verify(logService).getLogsByQuery(eq("user-def"), eq("UTC"), eq("social"), eq("SOCIAL"), isNull(), isNull());
    }
}
