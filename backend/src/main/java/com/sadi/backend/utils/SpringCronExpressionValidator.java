package com.sadi.backend.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.scheduling.support.CronExpression;

public class SpringCronExpressionValidator implements ConstraintValidator<ValidCron, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;

        try {
            CronExpression.parse(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
