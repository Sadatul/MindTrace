package com.sadi.backend.services.impls;

import com.sadi.backend.dtos.responses.CaregiversPatientsDTO;
import com.sadi.backend.entities.PatientCaregiver;
import com.sadi.backend.entities.PatientDetail;
import com.sadi.backend.entities.User;
import com.sadi.backend.repositories.PatientCaregiverRepository;
import com.sadi.backend.services.UserService;
import com.sadi.backend.services.abstractions.EmailService;
import com.sadi.backend.services.abstractions.PatientCaregiverMgmtService;
import com.sadi.backend.utils.CodeGenerator;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientCaregiverMgmtServiceRedisImpl implements PatientCaregiverMgmtService {
    private static final long OTP_EXPIRATION_MINUTES = 10;

    private final UserService userService;
    private final TelegramServiceRedisImpl telegramServiceRedisImpl;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final PatientCaregiverRepository patientCaregiverRepository;


    @Override
    public void sendOtpToAddPatient(String patientId) {
        processOtpRequest(patientId, SecurityUtils.getName(), OtpPurpose.ADD);
    }

    @Override
    public void sendOtpToRemovePatient(String caregiverId) {
        processOtpRequest(SecurityUtils.getName(), caregiverId, OtpPurpose.REMOVE);
    }

    private void processOtpRequest(String patientId, String caregiverId, OtpPurpose purpose) {
        User caregiver = userService.getUser(caregiverId);
        PatientDetail patientDetail = userService.getPatientDetail(patientId, true);
        User patient = patientDetail.getUser();
        User primaryContact = patientDetail.getPrimaryContact();

        if (purpose == OtpPurpose.ADD) {
            checkIfAlreadyLinked(patient, caregiver);
        } else {
            checkIfNotAlreadyLinked(patient, caregiver);
            if (primaryContact.getId().equals(caregiver.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Primary contacts cannot be removed from their own patient list");
            }
        }

        String otp = generateAndStoreOtp(patientId, caregiverId, purpose);

        String message = purpose.getMessage(patient.getName(), caregiver.getName(), otp);
        String subject = "OTP verification to " + (purpose == OtpPurpose.ADD ? "add" : "remove") + " patient";
        notifyPrimaryContactWithOtp(primaryContact, subject, message);
    }

    @Override
    public UUID addPatientToCaregiver(String patientId, String otp) {
        verifyOtp(patientId, SecurityUtils.getName(), otp, OtpPurpose.ADD);
        User caregiver = getCurrentUser();
        User patient = userService.getUser(patientId);

        Optional<PatientCaregiver> relationship = patientCaregiverRepository.findByPatientAndCaregiver(patient, caregiver);
        if (relationship.isPresent()) {
            PatientCaregiver existing = relationship.get();
            if (existing.getRemovedAt() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient is already added to the caregiver");
            }
            existing.setRemovedAt(null);
            return patientCaregiverRepository.save(existing).getId();
        }

        return patientCaregiverRepository.save(new PatientCaregiver(patient, caregiver)).getId();
    }

    @Override
    @Transactional
    public void deletePatientFromCaregiver(String patientId, String caregiverId) {
        User caregiver = userService.getUser(caregiverId);
        PatientDetail patientDetail = userService.getPatientDetail(patientId, true);
        User patient = patientDetail.getUser();
        User primaryContact = patientDetail.getPrimaryContact();

        PatientCaregiver relationship = getExistingRelationship(patient, caregiver);
        if(relationship.getRemovedAt() != null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found in caregiver's list");
        }

        if (primaryContact.getId().equals(caregiver.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Primary contacts cannot be removed from their own patient list");
        }
        relationship.setRemovedAt(Instant.now());

        notifyPrimaryContactOfRemoval(primaryContact, caregiver, patient);
    }

    @Override
    public List<CaregiversPatientsDTO> getAllCaregiversPatients(Boolean includeDeleted) {
        return patientCaregiverRepository.findByCaregiverId(SecurityUtils.getName(), includeDeleted);
    }

    @Override
    public List<CaregiversPatientsDTO> getAllPatientsCaregiver(Boolean includeDeleted) {
        return patientCaregiverRepository.findByPatientId(SecurityUtils.getName(), includeDeleted);
    }

    // ----------------- Private Helpers ------------------

    private User getCurrentUser() {
        return userService.getUser(SecurityUtils.getName());
    }

    private void checkIfAlreadyLinked(User patient, User caregiver) {
        patientCaregiverRepository.findByPatientAndCaregiver(patient, caregiver)
                .filter(r -> r.getRemovedAt() == null)
                .ifPresent(r -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient is already added to the caregiver");
                });
    }

    private void checkIfNotAlreadyLinked(User patient, User caregiver) {
        patientCaregiverRepository.findByPatientAndCaregiver(patient, caregiver)
                .filter(r -> r.getRemovedAt() == null)
                .ifPresentOrElse(r -> {}, () -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found in caregiver's list");
                });
    }

    private String generateAndStoreOtp(String patientId, String caregiverId, OtpPurpose purpose) {
        String otp = CodeGenerator.generateOtp();
        String key = purpose.getKeyPrefix() + patientId + ":" + caregiverId;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(OTP_EXPIRATION_MINUTES));
        return otp;
    }

    @Override
    public void verifyOtp(String patientId, String caregiverId, String otp, OtpPurpose purpose) {
        String key = purpose.getKeyPrefix() + patientId + ":" + caregiverId;
        Object storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp == null || !storedOtp.equals(otp)) {
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Invalid or expired OTP");
        }
        redisTemplate.delete(key);
    }

    private PatientCaregiver getExistingRelationship(User patient, User caregiver) {
        return patientCaregiverRepository.findByPatientAndCaregiver(patient, caregiver)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found in caregiver's list"));
    }

    private void notifyPrimaryContactWithOtp(User primaryContact, String emailSubject, String message) {
        if (primaryContact.getTelegramChatId() != null) {
            telegramServiceRedisImpl.sendMessage(primaryContact.getTelegramChatId(), message);
        } else {
            emailService.sendSimpleEmail(primaryContact.getEmail(), emailSubject, message);
        }
    }

    private void notifyPrimaryContactOfRemoval(User primaryContact, User caregiver, User patient) {
        String message = String.format("%s has removed %s from their patient list.", caregiver.getName(), patient.getName());

        if (primaryContact.getTelegramChatId() != null) {
            telegramServiceRedisImpl.sendMessage(primaryContact.getTelegramChatId(), message);
        } else {
            emailService.sendSimpleEmail(primaryContact.getEmail(), "Patient Removal Notification", message);
        }
    }
}

