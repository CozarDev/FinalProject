package com.proyectofinal.frontend.Api;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    public static final String BASE_URL = "http://10.0.2.2:8080/"; // Cambiar por la URL de tu API
    private static ApiClient instance;
    private ApiService apiService;
    private UserApiService userApiService;
    private DepartmentApiService departmentApiService;
    private EmployeeApiService employeeApiService;
    private Context context;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;

    private ApiClient(Context context) {
        this.context = context;
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // Logging para depuración
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);

        // Interceptor para añadir token JWT a las peticiones
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();

            // No añadir token para endpoints de auth
            if (original.url().toString().contains("/auth/")) {
                return chain.proceed(original);
            }

            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            String token = prefs.getString("JWT_TOKEN", "");

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
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create()) // Para respuestas String
                .addConverterFactory(GsonConverterFactory.create(gson)) // Para respuestas JSON
                .client(okHttpClient)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
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

    // Método para obtener el OkHttpClient configurado
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }
}