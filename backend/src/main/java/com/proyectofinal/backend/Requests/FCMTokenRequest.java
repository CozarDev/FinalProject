package com.proyectofinal.backend.Requests;

import jakarta.validation.constraints.NotBlank;

public class FCMTokenRequest {
    
    @NotBlank(message = "El ID del usuario es obligatorio")
    private String userId;
    
    @NotBlank(message = "El token FCM es obligatorio")
    private String fcmToken;
    
    private String deviceInfo;
    
    // Constructores
    public FCMTokenRequest() {}
    
    public FCMTokenRequest(String userId, String fcmToken, String deviceInfo) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.deviceInfo = deviceInfo;
    }
    
    // Getters y Setters
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
    }
    
    public String getDeviceInfo() {
        return deviceInfo;
    }
    
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
    
    @Override
    public String toString() {
        return "FCMTokenRequest{" +
                "userId='" + userId + '\'' +
                ", deviceInfo='" + deviceInfo + '\'' +
                '}';
    }
} 