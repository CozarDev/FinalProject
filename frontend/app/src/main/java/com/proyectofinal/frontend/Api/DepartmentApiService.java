package com.proyectofinal.frontend.Api;

import com.proyectofinal.frontend.Models.Department;
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

public interface DepartmentApiService {

    @GET("api/departments") // Incluir el prefijo api/ en la ruta
    Call<List<Department>> getAllDepartments();

    @POST("api/departments")
    Call<Department> createDepartment(@Body Department department);

    @PUT("api/departments/{id}")
    Call<Department> updateDepartment(@Path("id") String id, @Body Department department);

    @DELETE("api/departments/{id}")
    Call<Void> deleteDepartment(@Path("id") String id);
    
    // Buscar empleados para asignar como jefe
    @GET("api/departments/search-employees")
    Call<List<Employee>> searchEmployees(@Query("query") String query);
    
    // Asignar un jefe al departamento
    @POST("api/departments/{departmentId}/assign-manager")
    Call<Map<String, Object>> assignManager(
        @Path("departmentId") String departmentId, 
        @Body Map<String, String> request);
        
    // Obtener el departamento por ID del jefe
    @GET("api/departments/manager/{managerId}")
    Call<List<String>> getDepartmentByManagerId(@Path("managerId") String managerId);
} 