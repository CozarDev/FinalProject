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
                
                logger.info("Iniciando configuración de Firebase...");
                logger.info("Project ID configurado: {}", projectId);
                
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
                    logger.info("Buscando archivo firebase-service-account.json en classpath...");
                    InputStream serviceAccount = getClass().getClassLoader()
                            .getResourceAsStream("firebase-service-account.json");
                    
                    if (serviceAccount == null) {
                        logger.warn("No se encontró archivo firebase-service-account.json, intentando credenciales por defecto");
                        try {
                            credentials = GoogleCredentials.getApplicationDefault();
                            logger.info("Credenciales por defecto cargadas exitosamente");
                        } catch (Exception e) {
                            logger.error("Error cargando credenciales por defecto: {}", e.getMessage());
                            throw new RuntimeException("No se pudieron cargar las credenciales de Firebase", e);
                        }
                    } else {
                        logger.info("Archivo firebase-service-account.json encontrado, cargando credenciales...");
                        credentials = GoogleCredentials.fromStream(serviceAccount);
                        logger.info("Credenciales cargadas exitosamente desde archivo");
                    }
                }
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(projectId)
                        .build();
                
                FirebaseApp app = FirebaseApp.initializeApp(options);
                logger.info("Firebase inicializado exitosamente con project ID: {}", app.getOptions().getProjectId());
                
            } else {
                logger.info("Firebase ya está inicializado");
                FirebaseApp defaultApp = FirebaseApp.getInstance();
                logger.info("Project ID actual: {}", defaultApp.getOptions().getProjectId());
            }
            
        } catch (IOException e) {
            logger.error("Error de IO inicializando Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Error inicializando Firebase", e);
        } catch (Exception e) {
            logger.error("Error general inicializando Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Error inicializando Firebase", e);
        }
    }

    @Bean
    public Firestore firestore() {
        if (FirebaseApp.getApps().isEmpty()) {
            logger.warn("Firebase no está inicializado, no se puede crear bean Firestore");
            return null; // Firebase no inicializado
        }
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            logger.info("Bean Firestore creado exitosamente");
            return firestore;
        } catch (Exception e) {
            logger.error("Error creando bean Firestore: {}", e.getMessage(), e);
            return null;
        }
    }
} 