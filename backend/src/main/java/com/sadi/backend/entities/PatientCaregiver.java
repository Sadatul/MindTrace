package com.sadi.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "patient_caregiver")
public class PatientCaregiver {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "caregiver_id", nullable = false)
    private User caregiver;

    @Column(nullable = false, updatable = false)
    @CreatedDate
    private Instant createdAt;

    @Column
    // null means still active
    private Instant removedAt;

    public PatientCaregiver(User patient, User caregiver) {
        this.patient = patient;
        this.caregiver = caregiver;
        this.createdAt = Instant.now();
    }
}
