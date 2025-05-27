package com.proyectofinal.backend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "fcm_tokens")
public class FCMToken {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private String fcmToken;
    
    private String deviceInfo;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private boolean active;
    
    // Constructores
    public FCMToken() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }
    
    public FCMToken(String userId, String fcmToken, String deviceInfo) {
        this();
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.deviceInfo = deviceInfo;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "FCMToken{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
                ", active=" + active +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 