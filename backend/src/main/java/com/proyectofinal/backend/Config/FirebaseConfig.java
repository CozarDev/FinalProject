package com.proyectofinal.backend.Config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials.path:}")
    private String credentialsPath;

    @Value("${firebase.project.id:turnado-ab134}")
    private String projectId;

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Verificar si Firebase ya está inicializado
            if (FirebaseApp.getApps().isEmpty()) {
                
                // Configuración usando variables de entorno o archivo de credenciales
                String firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS");
                
                GoogleCredentials credentials;
                
                if (firebaseCredentials != null && !firebaseCredentials.isEmpty()) {
                    // Usar credenciales desde variable de entorno
                    logger.info("Inicializando Firebase con credenciales desde variable de entorno");
                    InputStream credentialsStream = new ByteArrayInputStream(firebaseCredentials.getBytes());
                    credentials = GoogleCredentials.fromStream(credentialsStream);
                } else {
                    // Usar credenciales desde archivo en classpath
                    logger.info("Inicializando Firebase con credenciales desde archivo");
                    InputStream serviceAccount = getClass().getClassLoader()
                            .getResourceAsStream("firebase-service-account.json");
                    
                    if (serviceAccount == null) {
                        logger.warn("No se encontró archivo firebase-service-account.json, usando credenciales por defecto");
                        credentials = GoogleCredentials.getApplicationDefault();
                    } else {
                        credentials = GoogleCredentials.fromStream(serviceAccount);
                    }
                }
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();
                
                FirebaseApp.initializeApp(options);
                logger.info("Firebase inicializado exitosamente");
                
            } else {
                logger.info("Firebase ya está inicializado");
            }
            
        } catch (IOException e) {
            logger.error("Error inicializando Firebase: {}", e.getMessage());
            throw new RuntimeException("Error inicializando Firebase", e);
        }
    }

    @Bean
    public Firestore firestore() {
        if (FirebaseApp.getApps().isEmpty()) {
            return null; // Firebase no inicializado
        }
        return FirestoreClient.getFirestore();
    }
} 