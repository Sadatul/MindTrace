package com.sadi.backend.controllers;

import com.sadi.backend.dtos.requests.AddPatientCaregiverReq;
import com.sadi.backend.dtos.responses.CaregiversPatientsDTO;
import com.sadi.backend.services.abstractions.PatientCaregiverMgmtService;
import com.sadi.backend.utils.SecurityUtils;
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
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UserCaregiverController {

    private final PatientCaregiverMgmtService patientCaregiverMgmtService;

    @GetMapping("/caregivers/patients/{userId}/otp")
    public ResponseEntity<Void> getPatientPrimaryContactOtp(
            @PathVariable String userId
    ){
        patientCaregiverMgmtService.sendOtpToAddPatient(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/caregivers/patients")
    public ResponseEntity<Void> addPatientToCaregiver(
           @Valid @RequestBody AddPatientCaregiverReq req
    ){
        log.debug("Received request to add patient to caregiver {}", req);
        UUID id = patientCaregiverMgmtService.addPatientToCaregiver(req.patientId(), req.otp());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/caregivers/patients/{patientId}")
    public ResponseEntity<Void> deletePatientFromCaregiver(
            @PathVariable String patientId
    ) {
        log.debug("Received request to delete patient {} from caregiver", patientId);
        patientCaregiverMgmtService.deletePatientFromCaregiver(patientId, SecurityUtils.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/caregivers/patients")
    public ResponseEntity<List<CaregiversPatientsDTO>> getAllCaregiversPatients(
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted
    )
    {
        log.debug("Received request to get all caregivers patients");
        List<CaregiversPatientsDTO> caregiversPatients = patientCaregiverMgmtService.getAllCaregiversPatients(includeDeleted);
        return ResponseEntity.ok(caregiversPatients);
    }

    @GetMapping("/users/caregivers/{caregiverId}/otp")
    public ResponseEntity<Void> sendOtpToRemoveCaregiver(
            @PathVariable String caregiverId
    ) {
        log.debug("Received request to send OTP to remove caregiver {} from patient", caregiverId);
        patientCaregiverMgmtService.sendOtpToRemovePatient(caregiverId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/caregivers/{caregiverId}")
    public ResponseEntity<Void> deleteCaregiverFromPatient(
            @PathVariable String caregiverId,
            @RequestParam String otp
    ) {
        log.debug("Received request to delete caregiver {} from patient", caregiverId);
        String patientId = SecurityUtils.getName();
        patientCaregiverMgmtService.verifyOtp(patientId, caregiverId, otp, PatientCaregiverMgmtService.OtpPurpose.REMOVE);
        patientCaregiverMgmtService.deletePatientFromCaregiver(patientId, caregiverId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/caregivers")
    public ResponseEntity<List<CaregiversPatientsDTO>> getAllPatientsCaregivers(
            @RequestParam(required = false, defaultValue = "false") Boolean includeDeleted
    )
    {
        log.debug("Received request to get caregivers for a patient");
        List<CaregiversPatientsDTO> caregiversPatients = patientCaregiverMgmtService.getAllPatientsCaregiver(includeDeleted);
        return ResponseEntity.ok(caregiversPatients);
    }
}
