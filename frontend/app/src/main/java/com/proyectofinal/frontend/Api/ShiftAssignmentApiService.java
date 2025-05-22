package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Models.ShiftAssignment;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ShiftAssignmentApiService {
    
    @GET("api/shiftassignments")
    Call<List<ShiftAssignment>> getAllShiftAssignments();
    
    @GET("api/shiftassignments/{id}")
    Call<ShiftAssignment> getShiftAssignmentById(@Path("id") String id);
    
    @GET("api/shiftassignments/employee/{employeeId}")
    Call<List<ShiftAssignment>> getShiftAssignmentsByEmployee(@Path("employeeId") String employeeId);
    
    @GET("api/shiftassignments/department/{departmentId}")
    Call<List<ShiftAssignment>> getShiftAssignmentsByDepartment(@Path("departmentId") String departmentId);
    
    @POST("api/shiftassignments")
    Call<ShiftAssignment> createShiftAssignment(@Body ShiftAssignment shiftAssignment);
    
    @POST("api/shiftassignments")
    Call<ShiftAssignment> createShiftAssignmentWithMap(@Body Map<String, Object> shiftAssignmentMap);
    
    @PUT("api/shiftassignments/{id}")
    Call<ShiftAssignment> updateShiftAssignment(@Path("id") String id, @Body ShiftAssignment shiftAssignment);
    
    @DELETE("api/shiftassignments/{id}")
    Call<Void> deleteShiftAssignment(@Path("id") String id);
    
    @GET("api/shiftassignments/active")
    Call<List<ShiftAssignment>> getActiveShiftAssignments(@Query("date") String date);
    
    @POST("api/shiftassignments/batch")
    Call<Map<String, Object>> createBatchAssignments(@Body List<ShiftAssignment> assignments);
} 