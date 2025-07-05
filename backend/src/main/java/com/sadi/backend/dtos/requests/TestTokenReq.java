package com.sadi.backend.dtos.requests;

import jakarta.validation.constraints.NotNull;

public record TestTokenReq(
    @NotNull String sub,
    @NotNull String email,
    @NotNull boolean emailVerified,
    @NotNull String name,
    String picture,
    @NotNull String scp // You can use String or List<String> depending on your needs
) {}
