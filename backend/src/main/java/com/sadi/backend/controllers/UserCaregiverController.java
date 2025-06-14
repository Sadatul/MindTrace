package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.AddPatientCaregiverReq;
import com.sadi.backend.dtos.responses.CaregiversPatientsDTO;
import com.sadi.backend.services.abstractions.PatientCaregiverMgmtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/v1/caregivers/patients")
@RequiredArgsConstructor
public class UserCaregiverController {

    private final PatientCaregiverMgmtService patientCaregiverMgmtService;

    @GetMapping("/{userId}/otp")
    public ResponseEntity<Void> getPatientPrimaryContactOtp(
            @PathVariable String userId
    ){
        patientCaregiverMgmtService.sendOtpToPatientPrimaryContact(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Void> addPatientToCaregiver(
           @Valid @RequestBody AddPatientCaregiverReq req
    ){
        log.debug("Received request to add patient to caregiver {}", req);
        UUID id = patientCaregiverMgmtService.addPatientToCaregiver(req.patientId(), req.otp());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatientFromCaregiver(
            @PathVariable String patientId
    ) {
        log.debug("Received request to delete patient {} from caregiver", patientId);
        patientCaregiverMgmtService.deletePatientFromCaregiver(patientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<CaregiversPatientsDTO>> getAllCaregiversPatients(
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted
    )
    {
        log.debug("Received request to get all caregivers patients");
        List<CaregiversPatientsDTO> caregiversPatients = patientCaregiverMgmtService.getAllCaregiversPatients(includeDeleted);
        return ResponseEntity.ok(caregiversPatients);
    }
}
