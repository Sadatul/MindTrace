package com.sadi.backend.dtos.requests;

import com.sadi.backend.enums.Gender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRegistrationRequest(
        @NotNull(message = "Name can't be null")
        @Size(min = 1, max = 255, message = "name length must be between 1 to 255")
        String name,
        @NotNull(message = "dob can't be null")
        @Past
        LocalDate dob,
        @NotNull(message = "Name can't be null")
        Gender gender,
        @NotNull(message = "primary contact can't be null")
        String primaryContact,
        @NotNull(message = "otp can't be null")
        String otp
) {
}
