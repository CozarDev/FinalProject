package com.proyectofinal.frontend.Services;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.proyectofinal.frontend.Models.Incidence;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.IncidenceRetrofitService;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidenceApiService {
    
    private static final String TAG = "IncidenceApiService";
    private static final String BASE_URL = "/api/incidences";
    
    private final ApiClient apiClient;
    private final IncidenceRetrofitService retrofitService;
    private final Gson gson;
    
    public IncidenceApiService(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.retrofitService = apiClient.getIncidenceRetrofitService();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }
    
    // **INTERFAZ PARA CALLBACKS**
    
    public interface IncidenceCallback {
        void onSuccess(Incidence incidence);
        void onError(String error);
    }
    
    public interface IncidenceListCallback {
        void onSuccess(List<Incidence> incidences);
        void onError(String error);
    }
    
    public interface IncidenceStatsCallback {
        void onSuccess(IncidenceStats stats);
        void onError(String error);
    }
    
    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
    
    // **MÉTODOS PARA EMPLEADOS DEL DEPARTAMENTO DE INCIDENCIAS**
    
    /**
     * Obtiene todas las incidencias pendientes (solo para empleados del departamento de incidencias)
     */
    public void getPendingIncidences(IncidenceCallback callback) {
        Call<List<Incidence>> call = retrofitService.getPendingIncidences();
        call.enqueue(new Callback<List<Incidence>>() {
            @Override
            public void onResponse(Call<List<Incidence>> call, Response<List<Incidence>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String error = "Error obteniendo incidencias pendientes: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<List<Incidence>> call, Throwable t) {
                String error = "Error de conexión obteniendo incidencias pendientes: " + t.getMessage();
                Log.e(TAG, error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Acepta una incidencia (solo para empleados del departamento de incidencias)
     */
    public void acceptIncidence(String incidenceId, SimpleCallback callback) {
        Call<Incidence> call = retrofitService.acceptIncidence(incidenceId);
        call.enqueue(new Callback<Incidence>() {
            @Override
            public void onResponse(Call<Incidence> call, Response<Incidence> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Incidencia aceptada exitosamente");
                } else {
                    String error = "Error aceptando incidencia: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Incidence> call, Throwable t) {
                String error = "Error de conexión aceptando incidencia: " + t.getMessage();
                Log.e(TAG, error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Resuelve una incidencia (solo para empleados del departamento de incidencias)
     */
    public void resolveIncidence(String incidenceId, Map<String, String> resolutionData, SimpleCallback callback) {
        Call<Incidence> call = retrofitService.resolveIncidence(incidenceId, resolutionData);
        call.enqueue(new Callback<Incidence>() {
            @Override
            public void onResponse(Call<Incidence> call, Response<Incidence> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Incidencia resuelta exitosamente");
                } else {
                    String error = "Error resolviendo incidencia: " + response.code();
                    Log.e(TAG, error);
                    callback.onError(error);
                }
            }

            @Override
            public void onFailure(Call<Incidence> call, Throwable t) {
                String error = "Error de conexión resolviendo incidencia: " + t.getMessage();
                Log.e(TAG, error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene incidencias asignadas al empleado actual
     */
    public void getAssignedIncidences(IncidenceListCallback callback) {
        Log.d(TAG, "Obteniendo incidencias asignadas");
        
        retrofitService.getAssignedIncidences().enqueue(new retrofit2.Callback<List<Incidence>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Incidence>> call, retrofit2.Response<List<Incidence>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Incidence> incidences = response.body();
                    Log.d(TAG, "Incidencias asignadas obtenidas: " + incidences.size());
                    callback.onSuccess(incidences);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error obteniendo incidencias asignadas: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<List<Incidence>> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error obteniendo incidencias asignadas: " + error);
                callback.onError(error);
            }
        });
    }
    
    // **MÉTODOS PARA JEFE DE DEPARTAMENTO DE INCIDENCIAS Y ADMIN**
    
    /**
     * Obtiene todas las incidencias (jefe de incidencias y admin)
     */
    public void getAllIncidences(IncidenceListCallback callback) {
        Log.d(TAG, "Obteniendo todas las incidencias");
        
        retrofitService.getAllIncidences().enqueue(new retrofit2.Callback<List<Incidence>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Incidence>> call, retrofit2.Response<List<Incidence>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Incidence> incidences = response.body();
                    Log.d(TAG, "Todas las incidencias obtenidas: " + incidences.size());
                    callback.onSuccess(incidences);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error obteniendo todas las incidencias: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<List<Incidence>> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error obteniendo todas las incidencias: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene incidencias activas (jefe de incidencias y admin)
     */
    public void getActiveIncidences(IncidenceListCallback callback) {
        Log.d(TAG, "Obteniendo incidencias activas");
        
        retrofitService.getActiveIncidences().enqueue(new retrofit2.Callback<List<Incidence>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Incidence>> call, retrofit2.Response<List<Incidence>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Incidence> incidences = response.body();
                    Log.d(TAG, "Incidencias activas obtenidas: " + incidences.size());
                    callback.onSuccess(incidences);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error obteniendo incidencias activas: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<List<Incidence>> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error obteniendo incidencias activas: " + error);
                callback.onError(error);
            }
        });
    }
    
    // **MÉTODOS PARA CREAR Y ELIMINAR**
    
    /**
     * Crea una nueva incidencia (admin o empleado normal)
     */
    public void createIncidence(String title, String description, Incidence.Priority priority, 
                               boolean isAdmin, String createdBy, IncidenceCallback callback) {
        Log.d(TAG, "Creando nueva incidencia: " + title);
        
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", title);
        requestData.put("description", description);
        requestData.put("priority", priority.name());
        
        // Solo para admin
        if (isAdmin && createdBy != null) {
            requestData.put("createdBy", createdBy);
        }
        
        retrofit2.Call<Incidence> call;
        if (isAdmin) {
            call = retrofitService.createIncidenceAsAdmin(requestData);
        } else {
            call = retrofitService.createIncidence(requestData);
        }
        
        call.enqueue(new retrofit2.Callback<Incidence>() {
            @Override
            public void onResponse(retrofit2.Call<Incidence> call, retrofit2.Response<Incidence> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Incidence incidence = response.body();
                    Log.d(TAG, "Incidencia creada exitosamente: " + incidence.getId());
                    callback.onSuccess(incidence);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error creando incidencia: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<Incidence> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error creando incidencia: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Elimina una incidencia
     */
    public void deleteIncidence(String incidenceId, SimpleCallback callback) {
        Log.d(TAG, "Eliminando incidencia: " + incidenceId);
        
        retrofitService.deleteIncidence(incidenceId).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Incidencia eliminada exitosamente: " + incidenceId);
                    callback.onSuccess();
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error eliminando incidencia: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error eliminando incidencia: " + error);
                callback.onError(error);
            }
        });
    }
    
    // **MÉTODOS PARA EMPLEADOS NORMALES Y JEFES DE DEPARTAMENTO**
    
    /**
     * Obtiene incidencias creadas por el empleado actual
     */
    public void getMyIncidences(IncidenceListCallback callback) {
        Log.d(TAG, "Obteniendo mis incidencias");
        
        retrofitService.getMyIncidences().enqueue(new retrofit2.Callback<List<Incidence>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Incidence>> call, retrofit2.Response<List<Incidence>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Incidence> incidences = response.body();
                    Log.d(TAG, "Mis incidencias obtenidas: " + incidences.size());
                    callback.onSuccess(incidences);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error obteniendo mis incidencias: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<List<Incidence>> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error obteniendo mis incidencias: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene incidencias del departamento (para jefes de departamento)
     */
    public void getDepartmentIncidences(IncidenceListCallback callback) {
        Log.d(TAG, "Obteniendo incidencias del departamento");
        
        retrofitService.getDepartmentIncidences().enqueue(new retrofit2.Callback<List<Incidence>>() {
            @Override
            public void onResponse(retrofit2.Call<List<Incidence>> call, retrofit2.Response<List<Incidence>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Incidence> incidences = response.body();
                    Log.d(TAG, "Incidencias del departamento obtenidas: " + incidences.size());
                    callback.onSuccess(incidences);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error obteniendo incidencias del departamento: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<List<Incidence>> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error obteniendo incidencias del departamento: " + error);
                callback.onError(error);
            }
        });
    }
    
    // **MÉTODOS DE UTILIDAD**
    
    /**
     * Obtiene una incidencia específica por ID
     */
    public void getIncidenceById(String incidenceId, IncidenceCallback callback) {
        Log.d(TAG, "Obteniendo incidencia por ID: " + incidenceId);
        
        retrofitService.getIncidenceById(incidenceId).enqueue(new retrofit2.Callback<Incidence>() {
            @Override
            public void onResponse(retrofit2.Call<Incidence> call, retrofit2.Response<Incidence> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Incidence incidence = response.body();
                    Log.d(TAG, "Incidencia obtenida por ID: " + incidenceId);
                    callback.onSuccess(incidence);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error obteniendo incidencia por ID: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<Incidence> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error obteniendo incidencia por ID: " + error);
                callback.onError(error);
            }
        });
    }
    
    /**
     * Obtiene estadísticas de incidencias
     */
    public void getIncidenceStats(IncidenceStatsCallback callback) {
        Log.d(TAG, "Obteniendo estadísticas de incidencias");
        
        retrofitService.getIncidenceStats().enqueue(new retrofit2.Callback<IncidenceStats>() {
            @Override
            public void onResponse(retrofit2.Call<IncidenceStats> call, retrofit2.Response<IncidenceStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    IncidenceStats stats = response.body();
                    Log.d(TAG, "Estadísticas de incidencias obtenidas");
                    callback.onSuccess(stats);
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    Log.e(TAG, "Error obteniendo estadísticas: " + error);
                    callback.onError(error);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<IncidenceStats> call, Throwable t) {
                String error = "Error de conexión: " + t.getMessage();
                Log.e(TAG, "Error obteniendo estadísticas: " + error);
                callback.onError(error);
            }
        });
    }
    
    // **CLASE PARA ESTADÍSTICAS**
    
    public static class IncidenceStats {
        private long pending;
        private long inProgress;
        private long resolved;
        
        public IncidenceStats() {}
        
        public IncidenceStats(long pending, long inProgress, long resolved) {
            this.pending = pending;
            this.inProgress = inProgress;
            this.resolved = resolved;
        }
        
        public long getPending() { return pending; }
        public void setPending(long pending) { this.pending = pending; }
        
        public long getInProgress() { return inProgress; }
        public void setInProgress(long inProgress) { this.inProgress = inProgress; }
        
        public long getResolved() { return resolved; }
        public void setResolved(long resolved) { this.resolved = resolved; }
        
        public long getTotal() { return pending + inProgress + resolved; }
        
        @Override
        public String toString() {
            return "IncidenceStats{" +
                    "pending=" + pending +
                    ", inProgress=" + inProgress +
                    ", resolved=" + resolved +
                    ", total=" + getTotal() +
                    '}';
        }
    }
} 