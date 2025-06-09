package com.sadi.backend.services.impls;

import com.google.protobuf.Timestamp;
import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.entities.Log;
import com.sadi.backend.repositories.LogRepository;
import com.sadi.backend.services.abstractions.LogService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QueryFactory;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.grpc.Points;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static io.qdrant.client.ConditionFactory.datetimeRange;
import static io.qdrant.client.ConditionFactory.matchKeyword;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final LogRepository logRepository;
    private final QdrantClient qdrantClient;
    private final EmbeddingModel embeddingModel;
    private final VectorStore logVectorStore;

    @Override
    @Transactional
    public void saveLog(Log log) {
        Log savedLog = logRepository.save(log);
        logVectorStore.add(List.of(new Document(savedLog.getDescription(), savedLog.getMetadata())));
    }

    @Override
    public List<LogDTO> getLogsByTimeStamp(String userId, String zoneId, Instant start, Instant end)
    {
        List<Points.Condition> conditions = new ArrayList<>();
        conditions.add(matchKeyword("userId", userId));
        conditions.add(datetimeRange("createdAt",
                Points.DatetimeRange.newBuilder()
                        .setGte(Timestamp.newBuilder().setSeconds(start.getEpochSecond()).build())
                        .setLte(Timestamp.newBuilder().setSeconds(end.getEpochSecond()).build())
                        .build()
        ));
        Points.Filter filter = Points.Filter.newBuilder()
                .addAllMust(conditions)
                .build();
        try {
            Points.ScrollResponse res = qdrantClient.scrollAsync(
                    Points.ScrollPoints.newBuilder()
                            .setCollectionName("logs")
                            .setFilter(filter)
                            .setOrderBy(Points.OrderBy.newBuilder()
                                    .setKey("createdAt")
                                    .setDirection(Points.Direction.Asc)
                                    .build())
                            .setWithPayload(WithPayloadSelectorFactory.enable(true))
                            .build()
            ).get();
            List<LogDTO> logDtos = res.getResultList().stream()
                    .map(p -> new LogDTO(p, ZoneId.of(zoneId)))
                    .toList();
            log.debug("logs from getLogsByTimeStamp {}", logDtos);
            return logDtos;
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<LogDTO> getLogsByQuery(String userId, String zoneId, String queryString, String logType, Instant start, Instant end) {
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
