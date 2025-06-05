package com.sadi.backend.entities;

import com.sadi.backend.enums.LogType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LogType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Log(User user, LogType type, String description, Instant createdAt) {
        this.user = user;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Map<String, Object> getMetadata()
    {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", id.toString());
        metadata.put("userId", user.getId());
        metadata.put("createdAt", createdAt.toString());
        metadata.put("type", type.toString());
        return metadata;
    }
}
