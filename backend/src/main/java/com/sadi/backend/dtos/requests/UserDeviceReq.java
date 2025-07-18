package com.sadi.backend.dtos.requests;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserDeviceReq(
        @Size(min = 1, max = 255)
        @NotNull
        String token,

        @Size(min = 1, max = 255)
        @NotNull
        String deviceName,

        @Size(min = 1, max = 255)
        @NotNull
        String deviceId
) {
}
