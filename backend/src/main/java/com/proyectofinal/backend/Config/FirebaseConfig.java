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

    @Value("${firebase.project.id:turnado-dev-local}")
    private String projectId;

    private boolean firebaseEnabled = false;

    @PostConstruct
    public void initializeFirebase() {
        try {
            // Verificar si Firebase ya está inicializado
            if (FirebaseApp.getApps().isEmpty()) {
                
                logger.info("Intentando inicializar Firebase...");
                logger.info("Project ID configurado: {}", projectId);
                
                // Configuración usando variables de entorno o archivo de credenciales
                String firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS");
                
                GoogleCredentials credentials = null;
                
                if (firebaseCredentials != null && !firebaseCredentials.isEmpty()) {
                    // Usar credenciales desde variable de entorno
                    logger.info("Inicializando Firebase con credenciales desde variable de entorno");
                    try {
                        InputStream credentialsStream = new ByteArrayInputStream(firebaseCredentials.getBytes());
                        credentials = GoogleCredentials.fromStream(credentialsStream);
                        logger.info("Credenciales desde variable de entorno cargadas exitosamente");
                    } catch (Exception e) {
                        logger.warn("Error cargando credenciales desde variable de entorno: {}", e.getMessage());
                    }
                } else {
                    // Intentar usar credenciales desde archivo en classpath
                    logger.info("Buscando archivo firebase-service-account.json en classpath...");
                    InputStream serviceAccount = getClass().getClassLoader()
                            .getResourceAsStream("firebase-service-account.json");
                    
                    if (serviceAccount != null) {
                        try {
                            logger.info("Archivo firebase-service-account.json encontrado, cargando credenciales...");
                            credentials = GoogleCredentials.fromStream(serviceAccount);
                            logger.info("Credenciales cargadas exitosamente desde archivo");
                        } catch (Exception e) {
                            logger.warn("Error cargando credenciales desde archivo: {}", e.getMessage());
                        }
                    } else {
                        logger.info("No se encontró archivo firebase-service-account.json");
                        
                        // Intentar credenciales por defecto solo si estamos en Cloud Run
                        String isCloudRun = System.getenv("K_SERVICE");
                        if (isCloudRun != null) {
                            logger.info("Detectado Cloud Run, intentando credenciales por defecto...");
                            try {
                                credentials = GoogleCredentials.getApplicationDefault();
                                logger.info("Credenciales por defecto cargadas exitosamente");
                            } catch (Exception e) {
                                logger.warn("Error cargando credenciales por defecto: {}", e.getMessage());
                            }
                        } else {
                            logger.info("No estamos en Cloud Run, Firebase será deshabilitado");
                        }
                    }
                }
                
                // Si tenemos credenciales, inicializar Firebase
                if (credentials != null) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(credentials)
                            .setProjectId(projectId)
                            .build();
                    
                    FirebaseApp app = FirebaseApp.initializeApp(options);
                    firebaseEnabled = true;
                    logger.info("✅ Firebase inicializado exitosamente con project ID: {}", app.getOptions().getProjectId());
                } else {
                    firebaseEnabled = false;
                    logger.warn("⚠️  Firebase NO inicializado - Las notificaciones push no estarán disponibles");
                    logger.info("💡 Para habilitar Firebase, configura firebase-service-account.json o variables de entorno");
                }
                
            } else {
                firebaseEnabled = true;
                logger.info("Firebase ya está inicializado");
                FirebaseApp defaultApp = FirebaseApp.getInstance();
                logger.info("Project ID actual: {}", defaultApp.getOptions().getProjectId());
            }
            
        } catch (Exception e) {
            firebaseEnabled = false;
            logger.error("❌ Error inicializando Firebase (continuando sin notificaciones): {}", e.getMessage());
            logger.info("💡 El backend funcionará normalmente, pero sin notificaciones push");
        }
        
        // Mostrar estado final
        if (firebaseEnabled) {
            logger.info("🔥 Firebase: HABILITADO - Notificaciones push disponibles");
        } else {
            logger.info("🚫 Firebase: DESHABILITADO - Solo funciones básicas disponibles");
        }
    }

    @Bean
    public Firestore firestore() {
        if (!firebaseEnabled || FirebaseApp.getApps().isEmpty()) {
            logger.info("Firebase no está disponible, bean Firestore no creado");
            return null;
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
    
    public boolean isFirebaseEnabled() {
        return firebaseEnabled;
    }
} 