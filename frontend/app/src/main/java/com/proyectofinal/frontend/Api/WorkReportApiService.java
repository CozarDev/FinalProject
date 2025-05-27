package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Models.WorkReport;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;

public interface WorkReportApiService {
    
    @POST("api/work-reports")
    Call<WorkReport> createWorkReport(@Body Map<String, Object> workReportData);
    
    @GET("api/work-reports")
    Call<List<Map<String, Object>>> getWorkReports();
    
    @GET("api/work-reports/{id}")
    Call<Map<String, Object>> getWorkReportById(@Path("id") String id);
    
    @PUT("api/work-reports/{id}")
    Call<Map<String, Object>> updateWorkReport(@Path("id") String id, @Body Map<String, Object> workReportData);
    
    @DELETE("api/work-reports/{id}")
    Call<Map<String, String>> deleteWorkReport(@Path("id") String id);
} 