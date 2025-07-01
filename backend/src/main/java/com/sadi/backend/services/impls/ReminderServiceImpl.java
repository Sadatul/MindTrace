package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.entities.Reminder;
import com.sadi.backend.entities.User;
import com.sadi.backend.repositories.ReminderRepository;
import com.sadi.backend.services.abstractions.ReminderSchedulerService;
import com.sadi.backend.services.abstractions.ReminderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final ReminderSchedulerService reminderSchedulerService;

    @Value("${scheduling.redis.delay}")
    private long redisDelay;

    @Override
    public void sendReminder(ReminderDTO req) {
        log.info("Sending Reminder Request {}", req);
    }

    @Override
    @Transactional
    public UUID createReminder(String userId, ReminderReq req) {
        User user = new User(userId);

        Instant nextExecution = reminderSchedulerService.getNextExecution(req.getCronExpression(), ZoneId.of(req.getZoneId()))
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.CONFLICT, "Invalid cron expression or timezone.")
                );
        Reminder reminder = new Reminder(user, req.getReminderType(), req.getTitle(),
                req.getDescription(), req.getCronExpression(), req.getIsRecurring(),
                nextExecution.toEpochMilli(), req.getZoneId(),
                reminderSchedulerService.isReminderScheduled(req.getCronExpression(), ZoneId.of(req.getZoneId())));

        Reminder savedReminder = reminderRepository.save(reminder);
        reminderSchedulerService.scheduleReminder(
                new ReminderDTO(savedReminder)
        );
        return savedReminder.getId();
    }

    @Override
    @Transactional
    public void deleteReminder(String userId, UUID id, boolean isOwner) {
        Reminder reminder = getReminder(id);
        if(isOwner)
            verifyOwner(userId, reminder);
        if (reminder.getIsScheduled())
            reminderSchedulerService.deleteScheduledReminder(reminder);
        reminderRepository.delete(reminder);
    }

    @Override
    public void updateNextExecution(UUID id, String cronExpression, String zoneId) {
        Optional<Instant> nextExec = reminderSchedulerService.getNextExecution(cronExpression, ZoneId.of(zoneId));
        if (nextExec.isEmpty()) return;

        Optional<Reminder> reminder = reminderRepository.findById(id);
        if(reminder.isEmpty()) return;

        long delay = Duration.between(nextExec.get(), Instant.now()).toMillis();
        Reminder reminderToUpdate = reminder.get();

        if(delay > redisDelay) {
             reminderToUpdate.setIsScheduled(false);
        }
        reminderToUpdate.setNextExecution(nextExec.get().toEpochMilli());
        reminderRepository.save(reminderToUpdate);
    }

    @Override
    public void deleteReminderById(UUID id) {
        Optional<Reminder> reminder = reminderRepository.findById(id);
        if(reminder.isEmpty()) return;

        Reminder reminderToDelete = reminder.get();
        reminderRepository.delete(reminderToDelete);
    }

    private void verifyOwner(String userId, Reminder reminder) {
        if (!reminder.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this reminder.");
        }
    }

    public Reminder getReminder(UUID id) {
        return reminderRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found.")
        );
    }
}
