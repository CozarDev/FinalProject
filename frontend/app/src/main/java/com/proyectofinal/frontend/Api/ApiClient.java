package com.proyectofinal.frontend.Api;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proyectofinal.frontend.Config.ApiConfig;
import com.proyectofinal.frontend.Utils.TokenExpirationManager;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.io.IOException;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static ApiClient instance;
    private ApiService apiService;
    private UserApiService userApiService;
    private DepartmentApiService departmentApiService;
    private EmployeeApiService employeeApiService;
    private ShiftTypeApiService shiftTypeApiService;
    private ShiftAssignmentApiService shiftAssignmentApiService;
    private HolidayApiService holidayApiService;
    private IncidenceRetrofitService incidenceRetrofitService;
    private WorkReportApiService workReportApiService;
    private FCMApiService fcmApiService;
    private Context context;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private Handler mainHandler;

    // Constructor público para crear instancias simples
    public ApiClient(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        initializeClient();
    }

    private void initializeClient() {
        // Obtener la URL base desde la configuración
        ApiConfig apiConfig = ApiConfig.getInstance(context);
        String baseUrl = apiConfig.getBaseUrl();
        
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // Logging para depuración
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);

        // Interceptor para manejar respuestas de error de autenticación
        httpClient.addInterceptor(chain -> {
            Request request = chain.request();
            Response response = chain.proceed(request);
            
            // Si recibimos 401 (Unauthorized), probablemente el token expiró
            if (response.code() == 401) {
                Log.w(TAG, "Respuesta 401 recibida - Token posiblemente expirado");
                
                // Limpiar token expirado
                SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove("JWT_TOKEN");
                editor.remove("USER_ROLE");
                editor.remove("USER_ID");
                editor.apply();
                
                Log.i(TAG, "Token limpiado debido a respuesta 401");
                
                // Notificar en el hilo principal que el usuario debe hacer login
                mainHandler.post(() -> {
                    TokenExpirationManager.handleTokenExpiration(context);
                });
            }
            
            return response;
        });

        // Interceptor para añadir token JWT a las peticiones
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // No añadir token para endpoints de auth
            if (original.url().toString().contains("/auth/")) {
                Log.d(TAG, "Petición de autenticación, no se añade token JWT");
                return chain.proceed(original);
            }

            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("JWT_TOKEN", "");
            String role = prefs.getString("USER_ROLE", "UNKNOWN");

            // Registrar información de la solicitud para depuración
            Log.d(TAG, "URL de solicitud: " + original.url().toString());
            Log.d(TAG, "Método HTTP: " + original.method());
            Log.d(TAG, "Rol del usuario: " + role);
            
            if (token.isEmpty()) {
                Log.e(TAG, "Token JWT no encontrado en SharedPreferences");
            } else {
                Log.d(TAG, "Añadiendo token JWT al encabezado Authorization");
            }
            
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .method(original.method(), original.body());

            return chain.proceed(requestBuilder.build());
        });

        // Guardar el cliente HTTP para usarlo en otras partes
        okHttpClient = httpClient.build();

        // Configurar Gson
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(ScalarsConverterFactory.create()) // Para respuestas String
                .addConverterFactory(GsonConverterFactory.create(gson)) // Para respuestas JSON
                .client(okHttpClient)
                .build();

        apiService = retrofit.create(ApiService.class);
        
        Log.d(TAG, "ApiClient inicializado con URL base: " + baseUrl);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    // Método estático para obtener Retrofit directamente
    public static Retrofit getClient() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient no ha sido inicializado. Llama a getInstance(context) primero.");
        }
        return instance.retrofit;
    }

    public ApiService getApiService() {
        return apiService;
    }
    
    public UserApiService getUserApiService() {
        if (userApiService == null) {
            userApiService = retrofit.create(UserApiService.class);
        }
        return userApiService;
    }
    
    public DepartmentApiService getDepartmentApiService() {
        if (departmentApiService == null) {
            departmentApiService = retrofit.create(DepartmentApiService.class);
        }
        return departmentApiService;
    }
    
    public EmployeeApiService getEmployeeApiService() {
        if (employeeApiService == null) {
            employeeApiService = retrofit.create(EmployeeApiService.class);
        }
        return employeeApiService;
    }

    public ShiftTypeApiService getShiftTypeApiService() {
        if (shiftTypeApiService == null) {
            shiftTypeApiService = retrofit.create(ShiftTypeApiService.class);
        }
        return shiftTypeApiService;
    }
    
    public ShiftAssignmentApiService getShiftAssignmentApiService() {
        if (shiftAssignmentApiService == null) {
            shiftAssignmentApiService = retrofit.create(ShiftAssignmentApiService.class);
        }
        return shiftAssignmentApiService;
    }
    
    public HolidayApiService getHolidayApiService() {
        if (holidayApiService == null) {
            holidayApiService = retrofit.create(HolidayApiService.class);
        }
        return holidayApiService;
    }

    public IncidenceRetrofitService getIncidenceRetrofitService() {
        if (incidenceRetrofitService == null) {
            incidenceRetrofitService = retrofit.create(IncidenceRetrofitService.class);
        }
        return incidenceRetrofitService;
    }

    public WorkReportApiService getWorkReportApiService() {
        if (workReportApiService == null) {
            workReportApiService = retrofit.create(WorkReportApiService.class);
        }
        return workReportApiService;
    }
    
    public FCMApiService getFCMApiService() {
        if (fcmApiService == null) {
            fcmApiService = retrofit.create(FCMApiService.class);
        }
        return fcmApiService;
    }

    // Método para obtener el OkHttpClient configurado
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
    
    // Métodos para gestión de configuración de URL
    public String getCurrentBaseUrl() {
        return ApiConfig.getInstance(context).getBaseUrl();
    }
    
    public void updateBaseUrl(String newUrl) {
        ApiConfig.getInstance(context).setBaseUrl(newUrl);
        // Reinicializar el cliente con la nueva URL
        initializeClient();
        Log.i(TAG, "Cliente reinicializado con nueva URL: " + newUrl);
    }
    
    public void resetToDefaultUrl() {
        ApiConfig.getInstance(context).resetToDefault();
        // Reinicializar el cliente con la URL por defecto
        initializeClient();
        Log.i(TAG, "Cliente reinicializado con URL por defecto");
    }
    
    public boolean isDevelopmentMode() {
        return ApiConfig.getInstance(context).isDevelopmentMode();
    }

    // **INTERFAZ PARA CALLBACKS SIMPLES**
    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    // **MÉTODOS SIMPLES PARA COMPATIBILIDAD**
    
    public void get(String endpoint, ApiCallback callback) {
        String baseUrl = getCurrentBaseUrl();
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    mainHandler.post(() -> callback.onSuccess(responseBody));
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    mainHandler.post(() -> callback.onError(error));
                }
            }
        });
    }

    public void post(String endpoint, String jsonBody, ApiCallback callback) {
        String baseUrl = getCurrentBaseUrl();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonBody);
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    mainHandler.post(() -> callback.onSuccess(responseBody));
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    mainHandler.post(() -> callback.onError(error));
                }
            }
        });
    }

    public void delete(String endpoint, ApiCallback callback) {
        String baseUrl = getCurrentBaseUrl();
        Request request = new Request.Builder()
                .url(baseUrl + endpoint)
                .delete()
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    mainHandler.post(() -> callback.onSuccess(responseBody));
                } else {
                    String error = "Error: " + response.code() + " " + response.message();
                    mainHandler.post(() -> callback.onError(error));
                }
            }
        });
    }
}