package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.responses.CaregiversPatientsDTO;

import java.util.List;
import java.util.UUID;

public interface PatientCaregiverMgmtService {

    void sendOtpToPatientPrimaryContact(String userId);
    UUID addPatientToCaregiver(String patientId, String otp);
    void deletePatientFromCaregiver(String patientId);

    List<CaregiversPatientsDTO> getAllCaregiversPatients(Boolean includeDeleted);
}
