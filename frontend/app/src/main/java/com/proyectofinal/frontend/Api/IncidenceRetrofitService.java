package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Models.Incidence;
import com.proyectofinal.frontend.Services.IncidenceApiService;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface IncidenceRetrofitService {

    // **ENDPOINTS PARA EMPLEADOS DEL DEPARTAMENTO DE INCIDENCIAS**
    
    @GET("api/incidences/pending")
    Call<List<Incidence>> getPendingIncidences();
    
    @POST("api/incidences/{id}/accept")
    Call<Incidence> acceptIncidence(@Path("id") String incidenceId);
    
    @POST("api/incidences/{id}/resolve")
    Call<Incidence> resolveIncidence(@Path("id") String incidenceId);
    
    @GET("api/incidences/assigned-to-me")
    Call<List<Incidence>> getAssignedIncidences();
    
    // **ENDPOINTS PARA JEFE DE INCIDENCIAS Y ADMIN**
    
    @GET("api/incidences/all")
    Call<List<Incidence>> getAllIncidences();
    
    @GET("api/incidences/active")
    Call<List<Incidence>> getActiveIncidences();
    
    // **ENDPOINTS PARA CREAR Y ELIMINAR**
    
    @POST("api/incidences")
    Call<Incidence> createIncidenceAsAdmin(@Body Map<String, Object> incidenceData);
    
    @POST("api/incidences/create")
    Call<Incidence> createIncidence(@Body Map<String, Object> incidenceData);
    
    @DELETE("api/incidences/{id}")
    Call<Void> deleteIncidence(@Path("id") String incidenceId);
    
    // **ENDPOINTS PARA EMPLEADOS NORMALES Y JEFES DE DEPARTAMENTO**
    
    @GET("api/incidences/my-incidences")
    Call<List<Incidence>> getMyIncidences();
    
    @GET("api/incidences/department")
    Call<List<Incidence>> getDepartmentIncidences();
    
    // **ENDPOINTS DE UTILIDAD**
    
    @GET("api/incidences/{id}")
    Call<Incidence> getIncidenceById(@Path("id") String incidenceId);
    
    @GET("api/incidences/stats")
    Call<IncidenceApiService.IncidenceStats> getIncidenceStats();
} 