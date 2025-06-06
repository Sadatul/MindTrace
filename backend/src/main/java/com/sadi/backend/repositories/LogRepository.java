package com.sadi.backend.repositories;

import com.sadi.backend.entities.Log;
import com.sadi.backend.entities.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface LogRepository extends JpaRepository<Log, UUID> {
    List<Log> findByUserAndCreatedAtIsBetween(User user, Instant createdAtAfter, Instant createdAtBefore, Sort sort);
}
