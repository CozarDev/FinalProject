package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Requests.FCMTokenRequest;
import com.proyectofinal.backend.Services.FCMTokenService;
import com.proyectofinal.backend.Services.FirebaseMessagingService;
import com.proyectofinal.backend.Services.UserService;
import com.google.firebase.FirebaseApp;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/fcm")
@CrossOrigin(origins = "*")
public class FCMController {
    
    private static final Logger logger = LoggerFactory.getLogger(FCMController.class);
    
    @Autowired
    private FCMTokenService fcmTokenService;
    
    @Autowired(required = false)
    private FirebaseMessagingService firebaseMessagingService;
    
    @Autowired(required = false)
    private UserService userService;
    
    /**
     * Registrar token FCM de un usuario
     */
    @PostMapping("/register-token")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('DEPARTMENT_HEAD') or hasRole('ADMIN')")
    public ResponseEntity<String> registerFCMToken(@Valid @RequestBody FCMTokenRequest request) {
        try {
            String result = fcmTokenService.registerOrUpdateToken(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error registrando token FCM: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error registrando token FCM: " + e.getMessage());
        }
    }
    
    /**
     * Obtener estado del token FCM de un usuario
     */
    @GetMapping("/status/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE') or hasRole('DEPARTMENT_HEAD') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTokenStatus(@PathVariable String userId) {
        try {
            boolean hasActiveToken = fcmTokenService.hasActiveToken(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("hasActiveToken", hasActiveToken);
            
            if (hasActiveToken) {
                var tokenOpt = fcmTokenService.getActiveTokenByUserId(userId);
                if (tokenOpt.isPresent()) {
                    response.put("deviceInfo", tokenOpt.get().getDeviceInfo());
                    response.put("lastUpdated", tokenOpt.get().getUpdatedAt());
                }
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo estado del token: {}", e.getMessage());
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error obteniendo estado del token: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Endpoint de diagnóstico para verificar el estado de Firebase
     */
    @GetMapping("/firebase-status")
    public ResponseEntity<Map<String, Object>> getFirebaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Verificar si Firebase está inicializado
            boolean isInitialized = !FirebaseApp.getApps().isEmpty();
            status.put("firebase_initialized", isInitialized);
            
            if (isInitialized) {
                FirebaseApp app = FirebaseApp.getInstance();
                status.put("project_id", app.getOptions().getProjectId());
                status.put("app_name", app.getName());
                status.put("messaging_service_available", firebaseMessagingService != null);
            } else {
                status.put("error", "Firebase no está inicializado");
            }
            
            // Variables de entorno relevantes
            Map<String, String> envVars = new HashMap<>();
            envVars.put("FIREBASE_PROJECT_ID", System.getenv("FIREBASE_PROJECT_ID"));
            envVars.put("FIREBASE_CREDENTIALS_PATH", System.getenv("FIREBASE_CREDENTIALS_PATH"));
            envVars.put("FIREBASE_CREDENTIALS_exists", System.getenv("FIREBASE_CREDENTIALS") != null ? "YES" : "NO");
            status.put("environment_variables", envVars);
            
            logger.info("Estado de Firebase consultado: {}", status);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estado de Firebase: {}", e.getMessage(), e);
            status.put("error", "Error obteniendo estado: " + e.getMessage());
            return ResponseEntity.ok(status);
        }
    }
} 