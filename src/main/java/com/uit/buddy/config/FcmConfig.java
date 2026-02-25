package com.uit.buddy.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
@Slf4j
public class FcmConfig {

    @Value("${app.firebase.config-path}")
    private String configPath;

    @Value("${app.firebase.app-name}")
    private String appName;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        FirebaseApp firebaseApp = FirebaseApp.getApps().stream()
                .filter(app -> app.getName().equals(appName))
                .findFirst()
                .orElseGet(() -> {
                    try {
                        log.info("[Firebase] Initializing new Firebase App: {}", appName);
                        FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(
                                        new ClassPathResource(configPath).getInputStream()))
                                .build();
                        return FirebaseApp.initializeApp(options, appName);
                    } catch (IOException e) {
                        log.error("[Firebase] Error initializing Firebase: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                });

        return FirebaseMessaging.getInstance(firebaseApp);
    }
}