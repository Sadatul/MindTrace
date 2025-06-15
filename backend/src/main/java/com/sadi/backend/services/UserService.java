package com.sadi.backend.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.sadi.backend.dtos.requests.CaregiverRegistrationRequest;
import com.sadi.backend.dtos.requests.PatientRegistrationRequest;
import com.sadi.backend.entities.PatientCaregiver;
import com.sadi.backend.entities.PatientDetail;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.Role;
import com.sadi.backend.repositories.PatientDetailRepository;
import com.sadi.backend.repositories.UserRepository;
import com.sadi.backend.services.abstractions.UserVerificationService;
import com.sadi.backend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.sadi.backend.repositories.PatientCaregiverRepository;

import java.util.Collections;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PatientDetailRepository patientDetailRepository;
    private final UserVerificationService userVerificationService;
    private final PatientCaregiverRepository patientCaregiverRepository;

    public UserService(UserRepository userRepository, PatientDetailRepository patientDetailRepository, UserVerificationService userVerificationService, PatientCaregiverRepository patientCaregiverRepository) {
        this.userRepository = userRepository;
        this.patientDetailRepository = patientDetailRepository;
        this.userVerificationService = userVerificationService;
        this.patientCaregiverRepository = patientCaregiverRepository;
    }

    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    public User getUser(String id) {
        return userRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );
    }


    public String registerCareGiver(CaregiverRegistrationRequest req) {
        String id = SecurityUtils.getName();
        Jwt jwt = SecurityUtils.getPrinciple();
        String email = jwt.getClaim("email");
        if(existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        User user = new User(
                id,
                email,
                req.name(),
                Role.CAREGIVER,
                req.profilePicture(),
                req.dob(),
                req.gender()
            );

        addScope(id, Role.CAREGIVER);
        return userRepository.save(user).getId();
    }

    @Transactional
    public String registerPatient(@Valid PatientRegistrationRequest req) {
        String id = SecurityUtils.getName();
        Jwt jwt = SecurityUtils.getPrinciple();
        String email = jwt.getClaim("email");
        if(existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
        if(!userVerificationService.verifyOtp(req.primaryContact(), req.otp())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP not valid");
        }

        User user = new User(
                id,
                email,
                req.name(),
                Role.PATIENT,
                req.profilePicture(),
                req.dob(),
                req.gender()
        );
        User saveUser = userRepository.save(user);

        User primaryContact = getUser(req.primaryContact());
        patientDetailRepository.save(new PatientDetail(saveUser, primaryContact));
        patientCaregiverRepository.save(new PatientCaregiver(saveUser, primaryContact));

        addScope(id, Role.PATIENT);
        return id;
    }

    public void addScope(String uuid, Role role){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        try {
            firebaseAuth.setCustomUserClaims(uuid, Collections.singletonMap("scp", role.toString()));
        } catch (FirebaseAuthException e) {
            log.error("Error adding scope to user in firebase: {}", e.getMessage());
            throw new InternalError("Error adding scope to user in firebase");
        }
    }

    public void verifyCaregiver(String patientId, String userId) {
        patientCaregiverRepository.findByPatientAndCaregiver(new User(patientId), new User(userId))
                .filter(r -> r.getRemovedAt() == null)
                .ifPresentOrElse(r -> {}, () -> {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Forbidden");
                });
    }

    public PatientDetail getPatientDetail(String userId, Boolean fetchUser) {
        if(!fetchUser)
            return patientDetailRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        return patientDetailRepository.getPatientDetailWithUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }
}
