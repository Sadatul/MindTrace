package com.sadi.backend.specifications;

import com.sadi.backend.entities.Log;
import com.sadi.backend.entities.Reminder;
import com.sadi.backend.enums.LogType;
import com.sadi.backend.enums.ReminderType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

public class ReminderSpecification {
    public static Specification<Reminder> withType(ReminderType type) {
        return (root, query, cb) -> {
            if (type != null) {
                return cb.equal(root.get("type"), type);
            }
            return null;
        };
    }

    public static Specification<Reminder> withUserId(String userId) {
        return (root, query, cb) -> {
            if(userId == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error");
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Reminder> withNextExecutionInBetween(Instant startDate, Instant endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.between(root.get("nextExecution"), startDate.toEpochMilli(), endDate.toEpochMilli());
            }
            if(startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("nextExecution"), startDate.toEpochMilli());
            }
            if(endDate != null) {
                return cb.lessThanOrEqualTo(root.get("nextExecution"), endDate.toEpochMilli());
            }
            return null;
        };
    }

    public static Specification<Reminder> getSpecification(String userId, ReminderType type, Instant startDate, Instant endDate) {
        return Specification.where(withUserId(userId))
                .and(withType(type))
                .and(withNextExecutionInBetween(startDate, endDate));
    }
}