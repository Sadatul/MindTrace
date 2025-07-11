package com.sadi.backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "user_devices")
public class UserDevice {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private String deviceName;

    @Column(nullable = false)
    private Instant lastUpdate;

    public UserDevice(String id, User user, String token, String deviceName) {
        this.id = id;
        this.user = user;
        this.token = token;
        this.deviceName = deviceName;
        this.lastUpdate = Instant.now();
    }
}
