package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Models.ShiftType;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ShiftTypeApiService {
    
    @GET("api/shifttypes")
    Call<List<ShiftType>> getAllShiftTypes();
    
    @GET("api/shifttypes/{id}")
    Call<ShiftType> getShiftTypeById(@Path("id") String id);
    
    @POST("api/shifttypes")
    Call<ShiftType> createShiftType(@Body ShiftType shiftType);
    
    @PUT("api/shifttypes/{id}")
    Call<ShiftType> updateShiftType(@Path("id") String id, @Body ShiftType shiftType);
    
    @DELETE("api/shifttypes/{id}")
    Call<Void> deleteShiftType(@Path("id") String id);
} 