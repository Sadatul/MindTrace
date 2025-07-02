package com.sadi.backend.entities;

import com.sadi.backend.dtos.BaseSortCategory;
import com.sadi.backend.enums.ReminderType;
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
@Table(name = "reminders")
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderType type;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false)
    private String cronExpression;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Boolean isRecurring;

    @Column(nullable = false)
    private Long nextExecution;

    @Column(nullable = false)
    private String zoneId;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isScheduled = false;

    public Reminder(User user, ReminderType type, String title, String description, String cronExpression, Boolean isRecurring,
                    Long nextExecution, String zoneId, Boolean isScheduled) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.description = description;
        this.cronExpression = cronExpression;
        this.isRecurring = isRecurring;
        this.nextExecution = nextExecution;
        this.zoneId = zoneId;
        createdAt = Instant.now();
        this.isScheduled = isScheduled;
    }

    @Getter
    @RequiredArgsConstructor
    public enum ReminderSortCategory implements BaseSortCategory {
        NEXT_EXECUTION("nextExecution");
        private final String value;
    }
}
