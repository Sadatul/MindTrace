package com.sadi.backend.specifications;

import com.sadi.backend.entities.Log;
import com.sadi.backend.enums.LogType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

public class LogSpecification {
    public static Specification<Log> withType(LogType type) {
        return (root, query, cb) -> {
            if (type != null) {
                return cb.equal(root.get("type"), type);
            }
            return null;
        };
    }

    public static Specification<Log> withUserId(String userId) {
        return (root, query, cb) -> {
            if(userId == null) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error");
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Log> withDateBetween(Instant startDate, Instant endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.between(root.get("createdAt"), startDate, endDate);
            }
            if(startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
            }
            if(endDate != null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
            }
            return null;
        };
    }

    public static Specification<Log> getSpecification(String userId, LogType type, Instant startDate, Instant endDate) {
        return Specification.where(withUserId(userId))
                .and(withType(type))
                .and(withDateBetween(startDate, endDate));
    }
}