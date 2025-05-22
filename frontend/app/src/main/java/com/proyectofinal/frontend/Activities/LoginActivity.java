package com.proyectofinal.frontend.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Auth.AuthRequest;
import com.proyectofinal.frontend.Auth.AuthResponse;
import com.proyectofinal.frontend.R;
import com.proyectofinal.frontend.Utils.JwtUtils;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Inicializar vistas
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Inicializar API client
        apiClient = ApiClient.getInstance(this);

        // Verificar si ya hay una sesión activa
        if (isLoggedIn()) {
            navigateToMainActivity();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                login();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Introduce tu nombre de usuario");
            isValid = false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Introduce tu contraseña");
            isValid = false;
        }

        return isValid;
    }

    private void login() {
        showLoading(true);

        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        AuthRequest authRequest = new AuthRequest(username, password);

        apiClient.getApiService().login(authRequest).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    String token = authResponse.getToken();
                    String role = authResponse.getRole();
                    String userId = authResponse.getUserId();
                    
                    Log.d(TAG, "Token recibido: " + token);
                    Log.d(TAG, "Rol recibido: " + role);
                    Log.d(TAG, "User ID recibido: " + userId);

                    // Si el backend no nos proporciona el rol directamente en la respuesta,
                    // lo extraemos del token
                    if (role == null || role.isEmpty()) {
                        role = JwtUtils.extractRole(token);
                        Log.d(TAG, "Rol extraído del token: " + role);
                    }

                    // Guardar token y rol
                    saveUserSession(token, role, userId, username);

                    // Navegar a MainActivity
                    navigateToMainActivity();
                } else {
                    // Error en la autenticación
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        Log.e(TAG, "Error de autenticación: " + errorBody);
                        Toast.makeText(LoginActivity.this,
                                "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                        Toast.makeText(LoginActivity.this,
                                "Error en la autenticación", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error de conexión", t);
                Toast.makeText(LoginActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserSession(String token, String role, String userId, String username) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("JWT_TOKEN", token);
        editor.putString("USER_ROLE", role);
        editor.putString("USER_ID", userId);
        editor.putString("USERNAME", username);
        editor.putBoolean("IS_LOGGED_IN", true);

        editor.apply();
        
        Log.d(TAG, "Sesión guardada - Rol: " + role + ", User ID: " + userId);
    }

    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getBoolean("IS_LOGGED_IN", false);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}