package com.sadi.backend.repositories;

import com.sadi.backend.dtos.responses.CaregiversPatientsDTO;
import com.sadi.backend.entities.PatientCaregiver;
import com.sadi.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientCaregiverRepository extends JpaRepository<PatientCaregiver, UUID> {
    Optional<PatientCaregiver> findByPatientAndCaregiver(User patient, User caregiver);

    @Query("select new com.sadi.backend.dtos.responses.CaregiversPatientsDTO(p.patient.id, p.patient.name, p.patient.gender, p.patient.profilePicture, p.createdAt, p.removedAt) from PatientCaregiver p where (p.caregiver.id = :caregiverId and (p.removedAt is null or :includeDeleted = true))")
    List<CaregiversPatientsDTO> findByCaregiverId(String caregiverId, Boolean includeDeleted);

    @Query("select new com.sadi.backend.dtos.responses.CaregiversPatientsDTO(p.caregiver.id, p.caregiver.name, p.caregiver.gender, p.caregiver.profilePicture, p.createdAt, p.removedAt) from PatientCaregiver p where (p.patient.id = :patientId and (p.removedAt is null or :includeDeleted = true))")
    List<CaregiversPatientsDTO> findByPatientId(String patientId, Boolean includeDeleted);
}
