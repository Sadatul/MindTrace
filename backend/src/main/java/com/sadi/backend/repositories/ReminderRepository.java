package com.sadi.backend.repositories;

import com.sadi.backend.entities.Reminder;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, UUID>, JpaSpecificationExecutor<Reminder> {

    List<Reminder> findReminderByNextExecutionBetweenAndIsScheduled(Long before, Long after, Boolean isScheduled);

    @Modifying
    @Transactional
    @Query("UPDATE Reminder r SET r.isScheduled = true WHERE r.id IN :ids")
    void markScheduled(List<UUID> ids);
}
