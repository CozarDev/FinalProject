package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Models.Employee;
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

public interface EmployeeApiService {

    @GET("api/employees")
    Call<List<Employee>> getAllEmployees(@Query("departmentId") String departmentId);
    
    @GET("api/employees")
    Call<List<Employee>> getAllEmployees();

    @GET("api/employees/me")
    Call<Employee> getCurrentEmployeeInfo();

    @POST("api/employees")
    Call<Employee> createEmployee(@Body Employee employee);

    @PUT("api/employees/{id}")
    Call<Employee> updateEmployee(@Path("id") String id, @Body Employee employee);

    @DELETE("api/employees/{id}")
    Call<Void> deleteEmployee(@Path("id") String id);

    @GET("api/employees/{id}/user-info")
    Call<Map<String, String>> getEmployeeUserInfo(@Path("id") String id);
} 