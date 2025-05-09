package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.RegistrationRequest;
import com.sadi.backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> addUser(@Valid @RequestBody RegistrationRequest req){
        log.debug("Received request to register a new user: {}", req.role());
        authService.registerUser(req.role());
        return ResponseEntity.ok().build();
    }
}
