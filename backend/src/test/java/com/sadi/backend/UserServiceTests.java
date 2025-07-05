package com.sadi.backend;

import com.sadi.backend.dtos.requests.CaregiverRegistrationRequest;
import com.sadi.backend.dtos.requests.PatientRegistrationRequest;
import com.sadi.backend.entities.PatientCaregiver;
import com.sadi.backend.entities.PatientDetail;
import com.sadi.backend.entities.User;
import com.sadi.backend.enums.Gender;
import com.sadi.backend.repositories.PatientCaregiverRepository;
import com.sadi.backend.repositories.PatientDetailRepository;
import com.sadi.backend.repositories.UserRepository;
import com.sadi.backend.services.UserService;
import com.sadi.backend.services.abstractions.UserVerificationService;
import com.sadi.backend.utils.SecurityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTests extends AbstractBaseIntegrationTest {

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PatientDetailRepository patientDetailRepository;
	@Autowired
	private PatientCaregiverRepository patientCaregiverRepository;
	@Autowired
	private UserVerificationService userVerificationService;

	// Track created IDs for cleanup
	private final List<String> createdUserIds = new ArrayList<>();
	private final List<String> createdPatientDetailIds = new ArrayList<>();
	private final List<UUID> createdPatientCaregiverIds = new ArrayList<>();

	@AfterEach
	void cleanup() {
		// Remove child/dependent entities first to avoid FK constraint issues
		for (UUID id : createdPatientCaregiverIds) {
			patientCaregiverRepository.deleteById(id);
		}
		for (String id : createdPatientDetailIds) {
			patientDetailRepository.deleteById(id);
		}
		for (String id : createdUserIds) {
			userRepository.deleteById(id);
		}
		createdUserIds.clear();
		createdPatientDetailIds.clear();
		createdPatientCaregiverIds.clear();
	}

	@Test
	void existsByEmail_shouldReturnTrueForExistingEmail() {
		assertTrue(userService.existsByEmail("alice@example.com"));
	}

	@Test
	void existsByEmail_shouldReturnFalseForNonExistingEmail() {
		assertFalse(userService.existsByEmail("nonexistent@example.com"));
	}

	@Test
	void getUser_shouldReturnUser_whenUserExists() {
		User user = userService.getUser("user-1");
		assertEquals("alice@example.com", user.getEmail());
	}

	@Test
	void getUser_shouldThrow_whenUserDoesNotExist() {
		assertThrows(ResponseStatusException.class, () -> userService.getUser("unknown-id"));
	}

	@Test
	void registerCareGiver_shouldRegisterNewCaregiver() {
		try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
			securityUtils.when(SecurityUtils::getName).thenReturn("user-5");
			Jwt jwt = Mockito.mock(Jwt.class);
			Mockito.when(jwt.getClaim("email")).thenReturn("eve@example.com");
			Mockito.when(jwt.getClaim("picture")).thenReturn(null);
			securityUtils.when(SecurityUtils::getPrinciple).thenReturn(jwt);

			CaregiverRegistrationRequest req = new CaregiverRegistrationRequest(
					"Eve Caregiver", LocalDate.of(1992, 2, 2), Gender.F
			);

			String id = userService.registerCareGiver(req);
			assertEquals("user-5", id);
			assertTrue(userRepository.existsByEmail("eve@example.com"));
			createdUserIds.add("user-5");
		}
	}

	@Test
	void registerCareGiver_shouldThrowIfEmailExists() {
		try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
			securityUtils.when(SecurityUtils::getName).thenReturn("user-2");
			Jwt jwt = Mockito.mock(Jwt.class);
			Mockito.when(jwt.getClaim("email")).thenReturn("bob@example.com");
			Mockito.when(jwt.getClaim("picture")).thenReturn(null);
			securityUtils.when(SecurityUtils::getPrinciple).thenReturn(jwt);

			CaregiverRegistrationRequest req = new CaregiverRegistrationRequest(
					"Bob Caregiver", LocalDate.of(1985, 5, 15), Gender.M
			);

			assertThrows(ResponseStatusException.class, () -> userService.registerCareGiver(req));
		}
	}

	@Test
	void registerPatient_shouldRegisterNewPatient() {
		try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
			securityUtils.when(SecurityUtils::getName).thenReturn("user-2");
			userVerificationService.cacheOtp();

			securityUtils.when(SecurityUtils::getName).thenReturn("user-6");
			Jwt jwt = Mockito.mock(Jwt.class);
			Mockito.when(jwt.getClaim("email")).thenReturn("frank@example.com");
			Mockito.when(jwt.getClaim("picture")).thenReturn(null);
			securityUtils.when(SecurityUtils::getPrinciple).thenReturn(jwt);

			PatientRegistrationRequest req = new PatientRegistrationRequest(
					"Frank Patient",  LocalDate.of(1995, 3, 3), Gender.M, "user-2", "123456"
			);

			String id = userService.registerPatient(req);
			assertEquals("user-6", id);
			assertTrue(userRepository.existsByEmail("frank@example.com"));
			assertTrue(patientDetailRepository.existsById("user-6"));
			// Find the patient-caregiver relation for cleanup
			Optional<PatientCaregiver> relation = patientCaregiverRepository.findByPatientAndCaregiver(new User("user-6"), new User("user-2"));
			relation.ifPresent(r -> createdPatientCaregiverIds.add(r.getId()));
			createdUserIds.add("user-6");
			createdPatientDetailIds.add("user-6");
		}
	}

	@Test
	void registerPatient_shouldThrowIfOtpInvalid() {
		try (MockedStatic<SecurityUtils> securityUtils = Mockito.mockStatic(SecurityUtils.class)) {
			securityUtils.when(SecurityUtils::getName).thenReturn("user-7");
			Jwt jwt = Mockito.mock(Jwt.class);
			Mockito.when(jwt.getClaim("email")).thenReturn("grace@example.com");
			Mockito.when(jwt.getClaim("picture")).thenReturn(null);
			securityUtils.when(SecurityUtils::getPrinciple).thenReturn(jwt);

			PatientRegistrationRequest req = new PatientRegistrationRequest(
					"Grace Patient", LocalDate.of(1996, 4, 4), Gender.F, "user-2", "wrong-otp"
			);

			assertThrows(ResponseStatusException.class, () -> userService.registerPatient(req));
		}
	}

	@Test
	void verifyCaregiver_shouldNotThrowForValidRelation() {
		assertDoesNotThrow(() -> userService.verifyCaregiver("user-1", "user-2"));
	}

	@Test
	void verifyCaregiver_shouldThrowForInvalidRelation() {
		assertThrows(ResponseStatusException.class, () -> userService.verifyCaregiver("user-1", "user-4"));
	}

	@Test
	void getPatientDetail_shouldReturnDetail_whenExists() {
		PatientDetail detail = userService.getPatientDetail("user-1", false);
		assertEquals("user-2", detail.getPrimaryContact().getId());
	}

	@Test
	void getPatientDetail_shouldThrow_whenNotExists() {
		assertThrows(ResponseStatusException.class, () -> userService.getPatientDetail("unknown", false));
	}
}
