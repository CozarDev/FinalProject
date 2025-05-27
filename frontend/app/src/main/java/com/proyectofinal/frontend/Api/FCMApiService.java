package com.proyectofinal.frontend.Api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface FCMApiService {
    
    @POST("api/fcm/register-token")
    Call<String> registerFCMToken(
        @Header("Authorization") String token,
        @Body FCMTokenRequest request
    );
    
    // Clase para el request del token FCM
    class FCMTokenRequest {
        private String userId;
        private String fcmToken;
        private String deviceInfo;
        
        public FCMTokenRequest(String userId, String fcmToken, String deviceInfo) {
            this.userId = userId;
            this.fcmToken = fcmToken;
            this.deviceInfo = deviceInfo;
        }
        
        // Getters y setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getFcmToken() { return fcmToken; }
        public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
        
        public String getDeviceInfo() { return deviceInfo; }
        public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    }
} 