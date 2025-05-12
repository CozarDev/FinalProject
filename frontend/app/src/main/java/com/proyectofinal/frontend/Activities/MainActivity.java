package com.proyectofinal.frontend.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnLogout;
    private TextView textViewWelcome;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        btnLogout = findViewById(R.id.btnLogout);
        textViewWelcome = findViewById(R.id.textViewWelcome);

        // Inicializar API client
        apiClient = ApiClient.getInstance(this);

        // Configurar mensaje de bienvenida
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "");
        if (!username.isEmpty()) {
            textViewWelcome.setText("Bienvenido/a, " + username);
        }

        // Configurar botón de logout
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", "");

        if (token.isEmpty()) {
            // Si no hay token, simplemente volvemos a la pantalla de login
            clearSessionAndRedirect();
            return;
        }

        // Llamar al endpoint de logout
        apiClient.getApiService().logout("Bearer " + token).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                // Independientemente de la respuesta, limpiamos la sesión
                clearSessionAndRedirect();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // En caso de error, informamos al usuario pero igualmente cerramos sesión localmente
                Toast.makeText(MainActivity.this, 
                        "Error al comunicarse con el servidor: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                clearSessionAndRedirect();
            }
        });
    }

    private void clearSessionAndRedirect() {
        // Limpiar datos de sesión
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Redirigir a la pantalla de login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Limpiar la pila de actividades para que el usuario no pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}