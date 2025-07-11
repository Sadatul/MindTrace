package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.CaregiverRegistrationRequest;
import com.sadi.backend.dtos.requests.PatientRegistrationRequest;
import com.sadi.backend.dtos.requests.UserDeviceReq;
import com.sadi.backend.dtos.responses.OtpResponse;
import com.sadi.backend.dtos.responses.StatusResponse;
import com.sadi.backend.services.UserService;
import com.sadi.backend.services.abstractions.UserDeviceService;
import com.sadi.backend.services.abstractions.UserVerificationService;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/auth")
@Slf4j
public class AuthController {
    private final UserService userService;
    private final UserVerificationService userVerificationService;
    private final UserDeviceService userDeviceService;

    public AuthController(UserService userService, UserVerificationService userVerificationService, UserDeviceService userDeviceService) {
        this.userService = userService;
        this.userVerificationService = userVerificationService;
        this.userDeviceService = userDeviceService;
    }

    @PostMapping()
    public ResponseEntity<StatusResponse> auth(){
        String id = SecurityUtils.getName();
        log.debug("Received auth request for user: {}", id);

        Jwt jwt = SecurityUtils.getPrinciple();
        String email = jwt.getClaim("email");
        if(userService.existsByEmail(email)){
            return ResponseEntity.ok(new StatusResponse("exists"));
        }

        return ResponseEntity.ok(new StatusResponse("new user"));
    }

    @PostMapping("/register/caregiver")
    public ResponseEntity<Void> registerCaregiver(@RequestBody @Valid CaregiverRegistrationRequest req){
        log.debug("Received caregiver register request for user: {}", req);
        String id = userService.registerCareGiver(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @GetMapping("/register/otp")
    public ResponseEntity<OtpResponse> registerOTP(){
        String otp = userVerificationService.cacheOtp();
        return ResponseEntity.ok(new OtpResponse(otp));
    }

    @PostMapping("/register/patient")
    public ResponseEntity<Void> registerPatient(@RequestBody @Valid PatientRegistrationRequest req){
        log.debug("Received patient register request for user: {}", req);
        String id = userService.registerPatient(req);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PostMapping("/register/device")
    public ResponseEntity<Void> registerUserDevice(@RequestBody @Valid UserDeviceReq req){
        log.debug("Received device register request for user: {}", req);
        String id = userDeviceService.addUserDevice(req, SecurityUtils.getName());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String deviceId){
        userDeviceService.deleteTokenByDeviceId(deviceId, SecurityUtils.getName());
        return ResponseEntity.noContent().build();
    }
}
