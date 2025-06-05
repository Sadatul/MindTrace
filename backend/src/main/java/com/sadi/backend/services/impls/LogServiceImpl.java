package com.sadi.backend.services.impls;

import com.sadi.backend.entities.Log;
import com.sadi.backend.repositories.LogRepository;
import com.sadi.backend.services.abstractions.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final LogRepository logRepository;

    @Override
    public void saveLog(Log log) {
        logRepository.save(log);
    }
}
