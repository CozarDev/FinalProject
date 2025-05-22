package com.proyectofinal.frontend.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Auth.AuthRequest;
import com.proyectofinal.frontend.R;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

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

        apiClient.getApiService().login(authRequest).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body();

                    // Guardar token
                    saveUserSession(token);

                    // Navegar a MainActivity
                    navigateToMainActivity();
                } else {
                    // Error en la autenticación
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Error desconocido";
                        Toast.makeText(LoginActivity.this,
                                "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Toast.makeText(LoginActivity.this,
                                "Error en la autenticación", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showLoading(false);
                Toast.makeText(LoginActivity.this,
                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserSession(String token) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("JWT_TOKEN", token);
        editor.putBoolean("IS_LOGGED_IN", true);

        editor.apply();
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