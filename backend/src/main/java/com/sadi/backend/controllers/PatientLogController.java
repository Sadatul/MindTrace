package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.CreateLogRequest;
import com.sadi.backend.dtos.requests.UpdateLogRequest;
import com.sadi.backend.dtos.responses.LogFullResponse;
import com.sadi.backend.entities.Log;
import com.sadi.backend.services.abstractions.LogService;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/v1/logs")
@RequiredArgsConstructor
@Slf4j
public class PatientLogController {

    private final LogService logService;

    @PostMapping
    ResponseEntity<Void> creatLog(
            @Valid @RequestBody CreateLogRequest req
    ) {
        log.debug("Received log creation request: {}", req);
        UUID id = logService.saveLog(SecurityUtils.getName(), req.type(),
                req.description(), Instant.now().minus(req.time(), ChronoUnit.MINUTES));
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(id).toUri();
        return  ResponseEntity.created(uri).build();
    }

    @PutMapping("/{id}")
    ResponseEntity<Void> updateLog(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLogRequest req
    ) {
        log.debug("Received log update request: {}", req);
        logService.updateLog(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteLog(@PathVariable UUID id) {
        log.debug("Received delete log request: {}", id);
        logService.deleteLog(id);
        return  ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    ResponseEntity<LogFullResponse> getLog(@PathVariable UUID id) {
        log.debug("Received get log request: {}", id);
        Log lg = logService.getLog(id);
        return  ResponseEntity.ok(new LogFullResponse(lg.getId(), lg.getType(), lg.getDescription(),
                lg.getCreatedAt()));
    }
}
