package com.uit.buddy.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FcmConfig {

  @Value("${app.firebase.config-path}")
  private String configPath;

  @Value("${app.firebase.app-name}")
  private String appName;

  @Bean
  public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
    return FirebaseMessaging.getInstance(firebaseApp);
  }

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    return FirebaseApp.getApps().stream()
        .filter(app -> app.getName().equals(appName))
        .findFirst()
        .orElseGet(
            () -> {
              try {
                log.info(
                    "[Firebase] Initializing Firebase App: {} from resources: {}",
                    appName,
                    configPath);

                InputStream serviceAccount = new FileInputStream(configPath);

                FirebaseOptions options =
                    FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                return FirebaseApp.initializeApp(options, appName);
              } catch (IOException e) {
                log.error(
                    "[Firebase] Critical: Could not read service account file at {}. Error: {}",
                    configPath,
                    e.getMessage());
                throw new RuntimeException("Firebase initialization failed", e);
              }
            });
  }
}
