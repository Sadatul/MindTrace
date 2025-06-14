package com.sadi.backend.repositories;

import com.sadi.backend.entities.PatientDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientDetailRepository extends JpaRepository<PatientDetail, String> {

}
