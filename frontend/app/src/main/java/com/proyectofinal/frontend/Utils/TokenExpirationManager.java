package com.proyectofinal.frontend.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.proyectofinal.frontend.Activities.LoginActivity;

public class TokenExpirationManager {
    private static final String TAG = "TokenExpirationManager";
    
    public static void handleTokenExpiration(Context context) {
        Log.w(TAG, "Manejando expiración de token");
        
        // Limpiar datos de sesión
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        // Mostrar mensaje al usuario
        Toast.makeText(context, "Tu sesión ha expirado. Por favor, inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
        
        // Redirigir al login
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        
        Log.i(TAG, "Usuario redirigido al login por token expirado");
    }
    
    public static boolean isTokenExpired(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", "");
        
        if (token.isEmpty()) {
            return true;
        }
        
        // Aquí podrías añadir lógica adicional para verificar la expiración
        // por ejemplo, decodificar el JWT y verificar la fecha de expiración
        
        return false;
    }
} 