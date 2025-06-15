package com.sadi.backend.services.impls;

import com.google.protobuf.Timestamp;
import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.dtos.requests.UpdateLogRequest;
import com.sadi.backend.entities.Log;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.LogType;
import com.sadi.backend.repositories.LogRepository;
import com.sadi.backend.services.UserService;
import com.sadi.backend.services.abstractions.LogService;
import com.sadi.backend.specifications.LogSpecification;
import com.sadi.backend.utils.SecurityUtils;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    private final UserService userService;

    @Override
    @Transactional
    public UUID saveLog(Log log) {
        Log savedLog = logRepository.save(log);
        logVectorStore.add(List.of(new Document(savedLog.getId().toString(),
                savedLog.getDescription(),
                savedLog.getMetadata())));
        return savedLog.getId();
    }

    @Override
    @Transactional
    public UUID saveLog(String userId, LogType type, String description, Instant createdAt) {
        User user = userService.getUser(userId);
        Log log = new Log(user, type, description, createdAt);
        return saveLog(log);
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

    @Override
    public Log getLog(UUID id) {
        return logRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log not found with id: " + id)
        );
    }

    @Override
    @Transactional
    public void updateLog(UUID id, UpdateLogRequest req) {
        Log lg = getLog(id);
        verifyOwner(lg, SecurityUtils.getName());

        lg.setDescription(req.description());
        lg.setType(req.type());

        logVectorStore.add(List.of(new Document(
                lg.getId().toString(),
                lg.getDescription(),
                lg.getMetadata()
        )));
    }

    @Override
    public void verifyOwner(Log lg, String userId) {
        if (!lg.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this log");
        }
    }

    @Override
    @Transactional
    public void deleteLog(UUID id) {
        Log lg = getLog(id);
        verifyOwner(lg, SecurityUtils.getName());
        logRepository.delete(lg);
        logVectorStore.delete(List.of(id.toString()));
    }

    public Page<Log> getLogs(String userId, LogType type, Instant start, Instant end, Pageable pageable) {
        Specification<Log> spec = LogSpecification.getSpecification(userId, type, start, end);
        return logRepository.findAll(spec, pageable);
    }
}
