package com.sadi.backend.configs;

import com.google.firebase.FirebaseApp;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirebaseConfig {

    @Bean
    FirebaseApp initializeApp() {
        return FirebaseApp.initializeApp();
    }
}
