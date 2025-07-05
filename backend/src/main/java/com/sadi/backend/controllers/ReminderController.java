package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.dtos.responses.ReminderFullRes;
import com.sadi.backend.entities.Reminder;
import com.sadi.backend.enums.ReminderType;
import com.sadi.backend.services.UserService;
import com.sadi.backend.services.abstractions.ReminderService;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/reminders")
@Slf4j
public class ReminderController {

    private final ReminderService reminderService;
    private final UserService userService;

    public ReminderController(ReminderService reminderService, UserService userService) {
        this.reminderService = reminderService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> createReminder(@Valid @RequestBody ReminderReq req)
    {
        String userId = req.getUserId();
        if(userId == null) {
            userId = SecurityUtils.getName();
        } else {
            userService.verifyCaregiver(userId, SecurityUtils.getName());
        }
        UUID uuid = reminderService.createReminder(userId, req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(uuid)
                .toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable UUID id) {
        log.debug("Received delete reminder request for reminder ID: {}", id);
        reminderService.deleteReminder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<ReminderFullRes>> getReminders(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Instant start,
            @RequestParam(required = false) Instant end,
            @RequestParam(required = false) ReminderType type,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction direction,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        log.debug("Get reminders based on following params - start: {}, end: {}, type: {}, sort: {}, page: {}, size: {}",
                start, end, type, direction, page, size);
        if(userId == null)
            userId = SecurityUtils.getName();
        else{
            userService.verifyCaregiver(userId, SecurityUtils.getName());
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, Reminder.ReminderSortCategory.NEXT_EXECUTION.getValue()));
        Page<Reminder> reminderPage = reminderService.getReminders(userId, type, start, end, pageable);
        List<ReminderFullRes> results = reminderPage.getContent().stream().map(ReminderFullRes::getReminderFullResFromReminder)
                .toList();
        Page<ReminderFullRes> pagedResult = new PageImpl<>(results, pageable, reminderPage.getTotalElements());
        return ResponseEntity.ok(new PagedModel<>(pagedResult));
    }

    @GetMapping("/{id}")
    ResponseEntity<ReminderFullRes> getReminder(@PathVariable UUID id) {
        log.debug("Received get reminder request: {}", id);
        Reminder reminder = reminderService.getReminder(id);
        reminderService.verifyOwnerOrCaregiver(SecurityUtils.getName(), reminder);
        return  ResponseEntity.ok(ReminderFullRes.getReminderFullResFromReminder(reminder));
    }
}
