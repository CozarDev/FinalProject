package com.proyectofinal.backend.Services;

import com.google.firebase.messaging.*;
import com.proyectofinal.backend.Models.FCMToken;
import com.proyectofinal.backend.Repositories.FCMTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class FirebaseMessagingService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseMessagingService.class);
    
    @Autowired
    private FCMTokenRepository fcmTokenRepository;
    
    /**
     * Enviar notificación a un usuario específico
     */
    public CompletableFuture<String> sendNotificationToUser(String userId, String title, String body, Map<String, String> data) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Buscar token activo del usuario
                var tokenOpt = fcmTokenRepository.findByUserIdAndActiveTrue(userId);
                if (tokenOpt.isEmpty()) {
                    logger.warn("No se encontró token FCM activo para el usuario: {}", userId);
                    return "No token found for user: " + userId;
                }
                
                FCMToken fcmToken = tokenOpt.get();
                return sendNotificationToToken(fcmToken.getFcmToken(), title, body, data);
                
            } catch (Exception e) {
                logger.error("Error enviando notificación al usuario {}: {}", userId, e.getMessage());
                return "Error: " + e.getMessage();
            }
        });
    }
    
    /**
     * Enviar notificación a múltiples usuarios
     */
    public CompletableFuture<List<String>> sendNotificationToUsers(List<String> userIds, String title, String body, Map<String, String> data) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> results = new ArrayList<>();
            
            try {
                // Obtener tokens de todos los usuarios
                List<FCMToken> tokens = fcmTokenRepository.findActiveTokensByUserIds(userIds);
                
                if (tokens.isEmpty()) {
                    logger.warn("No se encontraron tokens FCM activos para los usuarios: {}", userIds);
                    results.add("No tokens found for users");
                    return results;
                }
                
                // Enviar notificación a cada token
                for (FCMToken token : tokens) {
                    try {
                        String result = sendNotificationToToken(token.getFcmToken(), title, body, data);
                        results.add("User " + token.getUserId() + ": " + result);
                    } catch (Exception e) {
                        logger.error("Error enviando notificación al usuario {}: {}", token.getUserId(), e.getMessage());
                        results.add("User " + token.getUserId() + ": Error - " + e.getMessage());
                    }
                }
                
            } catch (Exception e) {
                logger.error("Error enviando notificaciones a múltiples usuarios: {}", e.getMessage());
                results.add("Error: " + e.getMessage());
            }
            
            return results;
        });
    }
    
    /**
     * Enviar notificación a un token específico
     */
    private String sendNotificationToToken(String token, String title, String body, Map<String, String> data) {
        try {
            // Construir el mensaje
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setIcon("ic_notification")
                                    .setColor("#6200EE")
                                    .setDefaultSound(true)
                                    .build())
                            .build());
            
            // Añadir datos personalizados si existen
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            Message message = messageBuilder.build();
            
            // Enviar el mensaje
            String response = FirebaseMessaging.getInstance().send(message);
            
            return "Success: " + response;
            
        } catch (FirebaseMessagingException e) {
            logger.error("Error enviando notificación FCM: {}", e.getMessage());
            
            // Si el token es inválido, marcarlo como inactivo
            if (MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode()) ||
                MessagingErrorCode.INVALID_ARGUMENT.equals(e.getMessagingErrorCode())) {
                
                logger.info("Token inválido detectado, desactivando: {}", token);
                deactivateToken(token);
            }
            
            throw new RuntimeException("Error enviando notificación: " + e.getMessage());
        }
    }
    
    /**
     * Enviar notificación de incidencia de alta prioridad
     */
    public CompletableFuture<List<String>> sendHighPriorityIncidenceNotification(List<String> userIds, String incidenceTitle, String incidenceDescription) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "HIGH_PRIORITY_INCIDENCE");
        data.put("message", "Nueva incidencia de alta prioridad: " + incidenceTitle);
        data.put("incidence_title", incidenceTitle);
        data.put("incidence_description", incidenceDescription);
        
        String title = "🚨 Incidencia de Alta Prioridad";
        String body = "Nueva incidencia: " + incidenceTitle;
        

        
        return sendNotificationToUsers(userIds, title, body, data);
    }
    
    /**
     * Enviar recordatorio de parte de trabajo
     */
    public CompletableFuture<String> sendWorkReportReminder(String userId, String shiftEndTime) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "WORK_REPORT_REMINDER");
        data.put("message", "No olvides subir tu parte de trabajo de la jornada que terminó a las " + shiftEndTime);
        data.put("shift_end_time", shiftEndTime);
        
        String title = "⏰ Recordatorio de Parte de Trabajo";
        String body = "No olvides subir tu parte de trabajo de la jornada que acabas de terminar.";
        

        
        return sendNotificationToUser(userId, title, body, data);
    }
    
    /**
     * Enviar notificación de asignación de turno
     */
    public CompletableFuture<String> sendShiftAssignmentNotification(String userId, String shiftDate, String shiftType) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "SHIFT_ASSIGNMENT");
        data.put("message", "Se te ha asignado un turno " + shiftType + " para el " + shiftDate);
        data.put("shift_date", shiftDate);
        data.put("shift_type", shiftType);
        
        String title = "📅 Nuevo Turno Asignado";
        String body = "Turno " + shiftType + " asignado para el " + shiftDate;
        

        
        return sendNotificationToUser(userId, title, body, data);
    }
    
    /**
     * Desactivar un token FCM
     */
    private void deactivateToken(String token) {
        try {
            var fcmTokenOpt = fcmTokenRepository.findByFcmTokenAndActiveTrue(token);
            if (fcmTokenOpt.isPresent()) {
                FCMToken fcmToken = fcmTokenOpt.get();
                fcmToken.setActive(false);
                fcmTokenRepository.save(fcmToken);
                logger.info("Token FCM desactivado: {}", token);
            }
        } catch (Exception e) {
            logger.error("Error desactivando token FCM: {}", e.getMessage());
        }
    }
} 