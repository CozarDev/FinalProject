package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.FCMToken;
import com.proyectofinal.backend.Repositories.FCMTokenRepository;
import com.proyectofinal.backend.Requests.FCMTokenRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

@Service
public class FCMTokenService {
    
    private static final Logger logger = LoggerFactory.getLogger(FCMTokenService.class);
    
    @Autowired
    private FCMTokenRepository fcmTokenRepository;
    
    /**
     * Registrar o actualizar token FCM de un usuario
     */
    public String registerOrUpdateToken(FCMTokenRequest request) {
        try {
            logger.info("Registrando/actualizando token FCM para usuario: {}", request.getUserId());
            
            // Buscar si ya existe un token activo para este usuario
            Optional<FCMToken> existingTokenOpt = fcmTokenRepository.findByUserIdAndActiveTrue(request.getUserId());
            
            if (existingTokenOpt.isPresent()) {
                // Actualizar token existente
                FCMToken existingToken = existingTokenOpt.get();
                existingToken.setFcmToken(request.getFcmToken());
                existingToken.setDeviceInfo(request.getDeviceInfo());
                
                fcmTokenRepository.save(existingToken);
                logger.info("Token FCM actualizado para usuario: {}", request.getUserId());
                
                return "Token FCM actualizado exitosamente";
            } else {
                // Crear nuevo token
                FCMToken newToken = new FCMToken(
                    request.getUserId(),
                    request.getFcmToken(),
                    request.getDeviceInfo()
                );
                
                fcmTokenRepository.save(newToken);
                logger.info("Nuevo token FCM registrado para usuario: {}", request.getUserId());
                
                return "Token FCM registrado exitosamente";
            }
            
        } catch (Exception e) {
            logger.error("Error registrando token FCM para usuario {}: {}", request.getUserId(), e.getMessage());
            throw new RuntimeException("Error registrando token FCM: " + e.getMessage());
        }
    }
    
    /**
     * Obtener token activo de un usuario
     */
    public Optional<FCMToken> getActiveTokenByUserId(String userId) {
        return fcmTokenRepository.findByUserIdAndActiveTrue(userId);
    }
    
    /**
     * Obtener todos los tokens activos de una lista de usuarios
     */
    public List<FCMToken> getActiveTokensByUserIds(List<String> userIds) {
        return fcmTokenRepository.findActiveTokensByUserIds(userIds);
    }
    
    /**
     * Desactivar todos los tokens de un usuario
     */
    public void deactivateAllUserTokens(String userId) {
        try {
            List<FCMToken> userTokens = fcmTokenRepository.findAllByUserId(userId);
            
            for (FCMToken token : userTokens) {
                if (token.isActive()) {
                    token.setActive(false);
                    fcmTokenRepository.save(token);
                }
            }
            
            logger.info("Todos los tokens FCM del usuario {} han sido desactivados", userId);
            
        } catch (Exception e) {
            logger.error("Error desactivando tokens del usuario {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Desactivar un token específico
     */
    public void deactivateToken(String fcmToken) {
        try {
            Optional<FCMToken> tokenOpt = fcmTokenRepository.findByFcmTokenAndActiveTrue(fcmToken);
            
            if (tokenOpt.isPresent()) {
                FCMToken token = tokenOpt.get();
                token.setActive(false);
                fcmTokenRepository.save(token);
                
                logger.info("Token FCM desactivado: {}", fcmToken);
            } else {
                logger.warn("No se encontró token FCM activo para desactivar: {}", fcmToken);
            }
            
        } catch (Exception e) {
            logger.error("Error desactivando token FCM {}: {}", fcmToken, e.getMessage());
        }
    }
    
    /**
     * Obtener todos los tokens activos
     */
    public List<FCMToken> getAllActiveTokens() {
        return fcmTokenRepository.findAllByActiveTrue();
    }
    
    /**
     * Verificar si un usuario tiene token FCM activo
     */
    public boolean hasActiveToken(String userId) {
        return fcmTokenRepository.findByUserIdAndActiveTrue(userId).isPresent();
    }
    
    /**
     * Obtener estado detallado del token FCM de un usuario
     */
    public Map<String, Object> getTokenStatus(String userId) {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // Buscar token activo
            Optional<FCMToken> activeTokenOpt = fcmTokenRepository.findByUserIdAndActiveTrue(userId);
            
            status.put("userId", userId);
            status.put("hasActiveToken", activeTokenOpt.isPresent());
            
            if (activeTokenOpt.isPresent()) {
                FCMToken token = activeTokenOpt.get();
                status.put("tokenExists", true);
                status.put("deviceInfo", token.getDeviceInfo());
                status.put("createdAt", token.getCreatedAt());
                status.put("updatedAt", token.getUpdatedAt());
                // No incluir el token completo por seguridad, solo los últimos caracteres
                String fcmToken = token.getFcmToken();
                if (fcmToken != null && fcmToken.length() > 10) {
                    status.put("tokenPreview", "..." + fcmToken.substring(fcmToken.length() - 10));
                } else {
                    status.put("tokenPreview", "Token disponible");
                }
            } else {
                status.put("tokenExists", false);
                status.put("message", "No se encontró token FCM activo para este usuario");
            }
            
            // Contar total de tokens (activos e inactivos) para este usuario
            List<FCMToken> allUserTokens = fcmTokenRepository.findAllByUserId(userId);
            status.put("totalTokens", allUserTokens.size());
            status.put("activeTokens", allUserTokens.stream().mapToInt(t -> t.isActive() ? 1 : 0).sum());
            
        } catch (Exception e) {
            logger.error("Error obteniendo estado del token para usuario {}: {}", userId, e.getMessage());
            status.put("error", "Error obteniendo estado del token: " + e.getMessage());
        }
        
        return status;
    }
} 