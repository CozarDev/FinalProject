package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Auth.AuthRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/auth/login")
    Call<String> login(@Body AuthRequest request);
    
    @POST("api/auth/logout")
    Call<String> logout(@Header("Authorization") String token);
}