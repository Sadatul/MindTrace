package com.sadi.backend.configs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "reminder")
public class ReminderSchedulerConfig {
    private Redis redis = new Redis();
    private Rabbit rabbit = new Rabbit();

    @Setter
    @Getter
    public static class Redis {
        private String reminderSetKey;
        private String reminderDetailsKey;
        private String deletedRemindersKey;
        private long maxDelay;

    }

    @Setter
    @Getter
    public static class Rabbit {
        private long maxDelay;

    }
}
