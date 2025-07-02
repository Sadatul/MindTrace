package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.requests.ReminderDTO;
import com.sadi.backend.dtos.requests.ReminderReq;
import com.sadi.backend.entities.Reminder;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.ReminderType;
import com.sadi.backend.repositories.ReminderRepository;
import com.sadi.backend.services.abstractions.ReminderSchedulerService;
import com.sadi.backend.services.abstractions.ReminderService;
import com.sadi.backend.specifications.ReminderSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

        log.debug("Updating next execution for Reminder {}", reminder);

        Reminder reminderToUpdate = reminder.get();
        reminderToUpdate.setNextExecution(nextExec.get().toEpochMilli());
        reminderToUpdate.setIsScheduled(reminderSchedulerService.isReminderScheduled(cronExpression, ZoneId.of(zoneId)));
        log.debug("Updating next execution for Reminder 2 {}", reminderToUpdate);
        reminderRepository.save(reminderToUpdate);
    }

    @Override
    public void deleteReminderById(UUID id) {
        Optional<Reminder> reminder = reminderRepository.findById(id);
        if(reminder.isEmpty()) return;

        Reminder reminderToDelete = reminder.get();
        reminderRepository.delete(reminderToDelete);
    }

    @Override
    public Page<Reminder> getReminders(String userId, ReminderType type, Instant start, Instant end, Pageable pageable) {
        Specification<Reminder> spec = ReminderSpecification.getSpecification(userId, type, start, end);
        return reminderRepository.findAll(spec, pageable);
    }

    private void verifyOwner(String userId, Reminder reminder) {
        if (!reminder.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this reminder.");
        }
    }

    @Override
    public Reminder getReminder(UUID id) {
        return reminderRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reminder not found.")
        );
    }
}
