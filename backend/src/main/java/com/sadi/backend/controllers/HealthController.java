package com.sadi.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/v1/public/health")
    public ResponseEntity<Map<String, String>> hello() {
        return ResponseEntity.ok(Collections.singletonMap("message", "I am healthy"));
    }
}
