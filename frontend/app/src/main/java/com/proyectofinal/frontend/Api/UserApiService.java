package com.proyectofinal.frontend.Api;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserApiService {
    // Obtener perfil de usuario
    @GET("api/users/profile")
    Call<Map<String, Object>> getUserProfile(@Header("Authorization") String token);
    
    // Obtener contraseña del usuario (para mostrarla en claro)
    @GET("api/users/{userId}/password")
    Call<Map<String, String>> getUserPassword(@Header("Authorization") String token, @Path("userId") String userId);
    
    // Actualizar perfil de usuario
    @PUT("api/users/profile")
    Call<Map<String, Object>> updateUserProfile(@Header("Authorization") String token, @Body Map<String, Object> profileData);
    
    // Verificar contraseña actual
    @POST("api/users/verify-password")
    Call<Map<String, Object>> verifyPassword(@Header("Authorization") String token, @Body Map<String, String> credentials);
} 