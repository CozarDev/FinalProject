package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Models.ShiftException;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HolidayApiService {
    
    /**
     * Obtiene todos los festivos nacionales
     */
    @GET("api/holidays/national")
    Call<List<ShiftException>> getNationalHolidays(@Query("year") Integer year);
    
    /**
     * Obtiene las vacaciones de un empleado específico
     */
    @GET("api/holidays/vacations/{employeeId}")
    Call<List<ShiftException>> getEmployeeVacations(
            @Path("employeeId") String employeeId,
            @Query("year") Integer year);
    
    /**
     * Importa festivos nacionales para un año específico
     */
    @POST("api/holidays/import-national")
    Call<Map<String, Object>> importNationalHolidays(@Query("year") int year);
    
    /**
     * Genera vacaciones automáticas para todos los empleados
     */
    @POST("api/holidays/auto-assign-vacations")
    Call<Map<String, Object>> autoAssignVacations(@Query("year") int year);
    
    /**
     * Genera vacaciones automáticas para un departamento específico
     */
    @POST("api/holidays/auto-assign-vacations/{departmentId}")
    Call<Map<String, Object>> autoAssignVacationsForDepartment(
            @Path("departmentId") String departmentId,
            @Query("year") int year);
    
    /**
     * Ejecuta el procesamiento de fin de año (importar festivos y generar vacaciones)
     */
    @POST("api/holidays/process-year")
    Call<Map<String, Object>> processYear(@Query("year") int year);
} 