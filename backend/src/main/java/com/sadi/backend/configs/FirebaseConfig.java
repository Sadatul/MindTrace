package com.sadi.backend.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_CREDENTIAL}")
    private String firebaseCredentialsBase64;

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            byte[] decodedBytes = Base64.getDecoder().decode(firebaseCredentialsBase64);

            ByteArrayInputStream credentialsStream = new ByteArrayInputStream(decodedBytes);
            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }
}
