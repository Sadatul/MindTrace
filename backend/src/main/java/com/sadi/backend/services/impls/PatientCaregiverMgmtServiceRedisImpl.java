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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientCaregiverMgmtServiceRedisImpl implements PatientCaregiverMgmtService {
    private final UserService userService;
    private final TelegramServiceRedisImpl telegramServiceRedisImpl;
    private final String OTP_KEY_PREFIX = "otp:patient:primaryContact:";
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;
    private final PatientCaregiverRepository  patientCaregiverRepository;

    @Override
    public void sendOtpToPatientPrimaryContact(String patientId) {
        String userId = SecurityUtils.getName();
        User caregiver = userService.getUser(userId);
        PatientDetail patientDetail = userService.getPatientDetail(patientId);
        User primaryContact = patientDetail.getPrimaryContact();
        User patient = patientDetail.getUser();
        String otp = CodeGenerator.generateOtp();
        String otpKey = OTP_KEY_PREFIX + patientId;
        redisTemplate.opsForValue().set(otpKey, otp,
                Duration.of(10, ChronoUnit.MINUTES));

        if(primaryContact.getTelegramChatId() != null) {
            telegramServiceRedisImpl.sendMessage(primaryContact.getTelegramChatId(),
                    String.format("""
                            %s is requesting to add %s as his patient.
                            Please verify the request by sending the OTP: %s
                            """, caregiver.getName(), patient.getName(), otp));
        }

        emailService.sendOtpEmail(primaryContact.getEmail(), otp);
    }

    private void verifyOtp(String userId, String otp) {
        String otpKey = OTP_KEY_PREFIX + userId;
        Object storedOtp = redisTemplate.opsForValue().get(otpKey);
        if(storedOtp == null || !storedOtp.equals(otp)) {
            throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Invalid or expired OTP");
        }
        redisTemplate.delete(otpKey);
    }

    public UUID addPatientToCaregiver(String patientId, String otp)
    {
        verifyOtp(patientId, otp);
        User caregiver = userService.getUser(SecurityUtils.getName());
        User patient = userService.getUser(patientId);
        Optional<PatientCaregiver> patientCaregiver = patientCaregiverRepository.findByPatientAndCaregiver(patient, caregiver);
        if(patientCaregiver.isPresent() ){
            if(patientCaregiver.get().getRemovedAt() == null){
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient is already added to the caregiver");
            }

            PatientCaregiver existingRelationship = patientCaregiver.get();
            existingRelationship.setRemovedAt(null);
            return patientCaregiverRepository.save(existingRelationship).getId();
        }
        return patientCaregiverRepository.save(
                new PatientCaregiver(patient, caregiver)
        ).getId();

    }

    @Override
    @Transactional
    public void deletePatientFromCaregiver(String patientId) {
        String caregiverId = SecurityUtils.getName();
        User caregiver = userService.getUser(caregiverId);
        User patient = userService.getUser(patientId);
        PatientDetail patientDetail = userService.getPatientDetail(patientId);
        User primaryContact = patientDetail.getPrimaryContact();

        Optional<PatientCaregiver> patientCaregiver = patientCaregiverRepository.findByPatientAndCaregiver(patient, caregiver);
        if(patientCaregiver.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found in caregiver's list");
        }

        PatientCaregiver existingRelationship = patientCaregiver.get();
        existingRelationship.setRemovedAt(Instant.now());

        if(primaryContact.getTelegramChatId() != null) {
            telegramServiceRedisImpl.sendMessage(primaryContact.getTelegramChatId(),
                    String.format("%s has removed %s from their patient list.",
                            caregiver.getName(), patient.getName()));
            return;
        }

        emailService.sendSimpleEmail(primaryContact.getEmail(),
                "Patient Removal Notification",
                String.format("%s has removed %s from their patient list.",
                        caregiver.getName(), patient.getName()));
    }

    @Override
    public List<CaregiversPatientsDTO> getAllCaregiversPatients(Boolean includeDeleted) {
        return patientCaregiverRepository.findByCaregiverId(SecurityUtils.getName(), includeDeleted);
    }


}
