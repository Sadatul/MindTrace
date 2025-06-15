package com.sadi.backend.repositories;

import com.sadi.backend.entities.PatientDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientDetailRepository extends JpaRepository<PatientDetail, String> {

    @Query("SELECT pd FROM PatientDetail pd JOIN FETCH pd.user u WHERE u.id = :id")
    Optional<PatientDetail> getPatientDetailWithUserById(String id);

}
