package com.sadi.backend.dtos.requests;

import jakarta.validation.constraints.NotNull;

public record AddPatientCaregiverReq(
        @NotNull(message = "Patient ID cannot be null")
        String patientId,
        @NotNull(message = "OTP cannot be null")
        String otp
) {
}
