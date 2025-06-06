package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.LogDTO;
import com.sadi.backend.entities.Log;
import com.sadi.backend.entities.User;
import com.sadi.backend.repositories.LogRepository;
import com.sadi.backend.services.abstractions.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final LogRepository logRepository;

    @Override
    public void saveLog(Log log) {
        logRepository.save(log);
    }

    @Override
    public List<LogDTO> getLogsByTimeStamp(String userId, String zoneId, Instant start, Instant end) {
        Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
        var zone =ZoneId.of(zoneId);
        List<LogDTO> list = logRepository.findByUserAndCreatedAtIsBetween(new User(userId), start, end, sort)
                .stream()
                .map(l -> new LogDTO(l.getDescription(), l.getCreatedAt().atZone(zone).toLocalDate(), l.getCreatedAt().atZone(zone).toLocalTime()))
                .toList();
        log.info("Returned list of logs: {}", list);
        return list;
    }
}
