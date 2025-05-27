package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.FCMToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FCMTokenRepository extends MongoRepository<FCMToken, String> {
    
    // Buscar token activo por usuario
    Optional<FCMToken> findByUserIdAndActiveTrue(String userId);
    
    // Buscar todos los tokens activos de un usuario
    List<FCMToken> findAllByUserIdAndActiveTrue(String userId);
    
    // Buscar por token FCM específico
    Optional<FCMToken> findByFcmTokenAndActiveTrue(String fcmToken);
    
    // Buscar todos los tokens activos
    List<FCMToken> findAllByActiveTrue();
    
    // Buscar tokens por lista de usuarios
    @Query("{'userId': {$in: ?0}, 'active': true}")
    List<FCMToken> findActiveTokensByUserIds(List<String> userIds);
    
    // Buscar todos los tokens de un usuario (activos e inactivos)
    List<FCMToken> findAllByUserId(String userId);
} 