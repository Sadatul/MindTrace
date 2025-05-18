package com.sadi.backend.entities;

import com.sadi.backend.enums.SubscriptionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "patient_details")
public class PatientDetail {
    @Id
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "primary_contact", nullable = false)
    private User primaryContact;

}
