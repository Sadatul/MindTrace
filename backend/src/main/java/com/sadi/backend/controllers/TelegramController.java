package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.TelegramRegistrationRequest;
import com.sadi.backend.dtos.responses.ValueResponse;
import com.sadi.backend.services.impls.TelegramServiceRedisImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/telegram")
public class TelegramController {

    private final TelegramServiceRedisImpl telegramService;

    // Endpoint to get UUID for registration
    @GetMapping("/register")
    public ResponseEntity<ValueResponse> getUUID()
    {
        String uuidForRegistration = telegramService.getUUIDForRegistration();
        return ResponseEntity.ok(new ValueResponse(uuidForRegistration));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerTelegramUser(
            @Valid @RequestBody TelegramRegistrationRequest req
            ) {
        log.debug("Received request to register telegram user: {}", req);
        telegramService.registerUser(req.chatId(), req.uuid());
        telegramService.sendMessage(req.chatId(), "Welcome to mindtracebot! Your chatId is " + req.chatId());
        return ResponseEntity.ok().build();
    }
}

