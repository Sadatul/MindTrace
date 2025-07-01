package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.services.abstractions.ReminderService;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reminders")
@Slf4j
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping
    public ResponseEntity<Void> createReminder(@Valid @RequestBody ReminderReq req)
    {
        String userId = SecurityUtils.getName();
        UUID uuid = reminderService.createReminder(userId, req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(uuid)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable UUID id) {
        String userId = SecurityUtils.getName();
        log.debug("Received delete reminder request for user: {}, reminder ID: {}", userId, id);
        reminderService.deleteReminder(userId, id, true);
        return ResponseEntity.noContent().build();
    }
}
