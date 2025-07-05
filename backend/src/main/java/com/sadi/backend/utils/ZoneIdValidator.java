package com.sadi.backend.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.ZoneId;

public class ZoneIdValidator implements ConstraintValidator<ValidZoneId, String> {

    @Override
    public boolean isValid(String zoneId, ConstraintValidatorContext context) {
        if (zoneId == null || zoneId.isBlank()) return false;
        try {
            ZoneId.of(zoneId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
