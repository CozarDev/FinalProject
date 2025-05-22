package com.proyectofinal.frontend.Utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class JwtUtils {
    private static final String TAG = "JwtUtils";

    /**
     * Extrae información del payload de un token JWT
     * @param jwt El token JWT
     * @return JSONObject con el payload, o null si hay error
     */
    public static JSONObject decodeJwtPayload(String jwt) {
        try {
            // Split the JWT into parts
            String[] parts = jwt.split("\\.");
            
            if (parts.length != 3) {
                Log.e(TAG, "Formato de token JWT inválido");
                return null;
            }
            
            // Decode the payload (second part)
            String payload = new String(Base64.decode(parts[1], Base64.URL_SAFE));
            
            // Parse as JSON
            return new JSONObject(payload);
        } catch (Exception e) {
            Log.e(TAG, "Error al decodificar token JWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el rol del usuario a partir del token JWT
     * @param jwt El token JWT
     * @return El rol del usuario, o "EMPLOYEE" por defecto
     */
    public static String extractRole(String jwt) {
        try {
            JSONObject payload = decodeJwtPayload(jwt);
            
            if (payload != null && payload.has("role")) {
                return payload.getString("role");
            }
            
            // Si no hay rol en el token, asumir EMPLOYEE por defecto
            return "EMPLOYEE";
        } catch (JSONException e) {
            Log.e(TAG, "Error al extraer rol del token: " + e.getMessage());
            return "EMPLOYEE";
        }
    }
    
    /**
     * Obtiene el ID del usuario a partir del token JWT
     * @param jwt El token JWT
     * @return El ID del usuario, o cadena vacía si hay error
     */
    public static String extractUserId(String jwt) {
        try {
            JSONObject payload = decodeJwtPayload(jwt);
            
            if (payload != null && payload.has("sub")) {
                return payload.getString("sub");
            }
            
            return "";
        } catch (JSONException e) {
            Log.e(TAG, "Error al extraer ID del token: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Obtiene el nombre de usuario a partir del token JWT
     * @param jwt El token JWT
     * @return El nombre de usuario, o cadena vacía si hay error
     */
    public static String extractUsername(String jwt) {
        try {
            JSONObject payload = decodeJwtPayload(jwt);
            
            if (payload != null && payload.has("username")) {
                return payload.getString("username");
            }
            
            return "";
        } catch (JSONException e) {
            Log.e(TAG, "Error al extraer nombre de usuario del token: " + e.getMessage());
            return "";
        }
    }
} 