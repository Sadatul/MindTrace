package com.sadi.backend.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ZoneIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidZoneId {
    String message() default "Invalid time zone ID";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
