package com.sadi.backend.services.impls;

import com.google.protobuf.Timestamp;
import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.entities.Log;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.LogType;
import com.sadi.backend.services.abstractions.LogService;
import com.sadi.backend.services.abstractions.LoggingTools;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QueryFactory;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.qdrant.client.ConditionFactory.datetimeRange;
import static io.qdrant.client.ConditionFactory.matchKeyword;

@Slf4j
@Component
public class LoggingToolsImpl implements LoggingTools {
    private final LogService logService;
    private final VectorStore logVectorStore;
    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;

    public LoggingToolsImpl(LogService logService, VectorStore logVectorStore, QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
        this.logService = logService;
        this.logVectorStore = logVectorStore;
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;
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
            Use this tool to retrieve logs for summarization within the specified start and end timestamps. Remember start and end must be in utc time format.
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

    @Override
    @Tool(description = """
            Use these tool to retrieve logs related to certain query. This tool allows you to filter events based on timestamp & log type.
            """)
    public List<LogDTO> getLogsForSpecificTopic(
            @ToolParam(description = "We will perform a similarity search based on this queryString")
            String queryString,
            @ToolParam(description = "Pass the type of log. Must be one of the following values (EATING, MEDICINE, SOCIAL, OUTINGS, BATHING)")
            String logType,
            @ToolParam(description = "Start time in utc format. Provide only if you want to filter based on timestamp", required = false)
            Instant start,
            @ToolParam(description = "End time in utc format. Provide only if you want to filter based on timestamp", required = false)
            Instant end,
            ToolContext toolContext
    ) {
        String userId = (String) toolContext.getContext().get("userId");
        String zoneId = (String) toolContext.getContext().get("zone");


        // Always filter by userId and logType
        List<Points.Condition> conditions = new ArrayList<>();
        conditions.add(matchKeyword("userId", userId));
        conditions.add(matchKeyword("type", logType));

        // Add timestamp filtering if both start and end are provided
        if (start != null && end != null) {
            conditions.add(datetimeRange("createdAt",
                    Points.DatetimeRange.newBuilder()
                            .setGte(Timestamp.newBuilder().setSeconds(start.getEpochSecond()).build())
                            .setLte(Timestamp.newBuilder().setSeconds(end.getEpochSecond()).build())
                            .build()
            ));
        }

        Points.Filter filter = Points.Filter.newBuilder()
                .addAllMust(conditions)
                .build();

        try {
            List<Points.ScoredPoint> points = qdrantClient.queryAsync(
                    Points.QueryPoints.newBuilder().setCollectionName("logs")
                            .setQuery(QueryFactory.nearest(embeddingModel.embed(queryString)))
                            .setWithPayload(WithPayloadSelectorFactory.enable(true))
                            .setFilter(filter)
                            .setLimit(10)
                            .build()
            ).get();
            List<LogDTO> logDtos = points.stream()
                    .map(p -> new LogDTO(p, ZoneId.of(zoneId)))
                    .collect(Collectors.toList());
            log.debug("logs from getLogsForSpecificTopic {}", logDtos);
            return logDtos;

        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }
}
