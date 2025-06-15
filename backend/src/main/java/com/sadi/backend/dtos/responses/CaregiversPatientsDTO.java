package com.sadi.backend.dtos.responses;

import com.sadi.backend.enums.Gender;

import java.time.Instant;

public record CaregiversPatientsDTO(
        String id,
        String name,
        Gender gender,
        String profilePicture,
        Instant removedAt
) {

}
