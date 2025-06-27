package com.sadi.backend.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;

public class ZoneIdValidator implements ConstraintValidator<ValidZoneId, ZoneId> {
    @Override
    public boolean isValid(ZoneId zoneId, ConstraintValidatorContext context) {
        if (zoneId == null) return false;
        try {
            ZoneId.of(zoneId.getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
