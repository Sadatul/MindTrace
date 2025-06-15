package com.sadi.backend.services.abstractions;

import com.sadi.backend.dtos.responses.CaregiversPatientsDTO;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public interface PatientCaregiverMgmtService {
    String ADD_PATIENT_OTP_MESSAGE = """
                %s is requesting to add %s as his patient.
                Please verify the request by sending the OTP: %s
                """;
    String REMOVE_PATIENT_OTP_MESSAGE = """
                patient %s is requesting to remove %s as his caregiver.
                Please verify the request by sending the OTP: %s
                """;

    @Getter
    enum OtpPurpose {
        ADD("otp:patient:add:"),
        REMOVE("otp:caregiver:remove:");

        private final String keyPrefix;
        OtpPurpose(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        public String getMessage(String patientName, String caregiverName, String otp) {
            return this == ADD ? String.format(ADD_PATIENT_OTP_MESSAGE, caregiverName, patientName, otp)
                               : String.format(REMOVE_PATIENT_OTP_MESSAGE, patientName, caregiverName, otp);
        }
    }

    void sendOtpToAddPatient(String userId);
    void sendOtpToRemovePatient(String caregiverId);
    UUID addPatientToCaregiver(String patientId, String otp);
    void deletePatientFromCaregiver(String patientId, String caregiverId);
    void verifyOtp(String patientId, String caregiverId, String otp, OtpPurpose purpose);
    List<CaregiversPatientsDTO> getAllCaregiversPatients(Boolean includeDeleted);
    List<CaregiversPatientsDTO> getAllPatientsCaregiver(Boolean includeDeleted);
}
