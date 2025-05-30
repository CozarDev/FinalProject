// 🔥 SERVICIO FIREBASE COMENTADO - DESCOMENTA SI QUIERES HABILITAR NOTIFICACIONES PUSH 🔥
// ==================================================================================
// INSTRUCCIONES PARA HABILITAR FIREBASE:
// 1. Configura google-services.json en frontend/app/
// 2. Descomenta las dependencias Firebase en build.gradle.kts
// 3. Descomenta el plugin google-services en build.gradle.kts
// 4. Descomenta TODO este archivo (quita /* y */)
// 5. Rebuild el proyecto
// ==================================================================================

/*
package com.proyectofinal.frontend.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.proyectofinal.frontend.Activities.MainActivity;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.FCMApiService;
import com.proyectofinal.frontend.R;

import java.util.Map;

public class TurnadoFirebaseMessagingService extends FirebaseMessagingService {
    
    private static final String TAG = "TurnadoFCMService";
    private static final String CHANNEL_ID = "turnado_notifications";
    private static final String CHANNEL_NAME = "Notificaciones Turnado";
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        // Verificar si el mensaje tiene datos
        if (!remoteMessage.getData().isEmpty()) {
            handleDataMessage(remoteMessage.getData());
        }
        
        // Verificar si el mensaje tiene notificación
        if (remoteMessage.getNotification() != null) {
            showNotification(
                remoteMessage.getNotification().getTitle(),
                remoteMessage.getNotification().getBody(),
                remoteMessage.getData()
            );
        }
    }
    
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        
        // Enviar el token al servidor
        sendTokenToServer(token);
        
        // Guardar el token localmente
        saveTokenLocally(token);
    }
    
    private void handleDataMessage(Map<String, String> data) {
        String type = data.get("type");
        
        if (type != null) {
            switch (type) {
                case "HIGH_PRIORITY_INCIDENCE":
                    handleHighPriorityIncidence(data);
                    break;
                case "WORK_REPORT_REMINDER":
                    handleWorkReportReminder(data);
                    break;
                case "SHIFT_ASSIGNMENT":
                    handleShiftAssignment(data);
                    break;
                default:
                    Log.w(TAG, "Tipo de mensaje no reconocido: " + type);
                    break;
            }
        }
    }
    
    private void handleHighPriorityIncidence(Map<String, String> data) {
        String title = "🚨 Incidencia de Alta Prioridad";
        String body = data.get("message");
        if (body == null) {
            body = "Se ha creado una nueva incidencia de alta prioridad que requiere atención inmediata.";
        }
        
        showNotification(title, body, data);
    }
    
    private void handleWorkReportReminder(Map<String, String> data) {
        String title = "⏰ Recordatorio de Parte de Trabajo";
        String body = data.get("message");
        if (body == null) {
            body = "No olvides subir tu parte de trabajo de la jornada que acabas de terminar.";
        }
        
        showNotification(title, body, data);
    }
    
    private void handleShiftAssignment(Map<String, String> data) {
        String title = "📅 Nuevo Turno Asignado";
        String body = data.get("message");
        if (body == null) {
            body = "Se te ha asignado un nuevo turno de trabajo. Revisa los detalles en la aplicación.";
        }
        
        showNotification(title, body, data);
    }
    
    private void showNotification(String title, String body, Map<String, String> data) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Añadir datos extra si es necesario
            if (data != null) {
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                0, 
                intent, 
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );
            
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body));
            
            NotificationManager notificationManager = 
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                // Verificar si las notificaciones están habilitadas
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (!notificationManager.areNotificationsEnabled()) {
                        Log.w(TAG, "Las notificaciones están deshabilitadas para esta app");
                        return;
                    }
                }
                
                // Verificar el canal de notificación
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);
                    if (channel == null) {
                        createNotificationChannel();
                    }
                }
                
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, notificationBuilder.build());
                
            } else {
                Log.e(TAG, "NotificationManager es null");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error mostrando notificación: " + e.getMessage(), e);
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de la aplicación Turnado");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    private void sendTokenToServer(String token) {
        // Obtener información del usuario
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");
        String jwtToken = prefs.getString("JWT_TOKEN", "");
        
        if (!userId.isEmpty() && !jwtToken.isEmpty()) {
            // Enviar el token al backend
            ApiClient apiClient = ApiClient.getInstance(this);
            
            // Crear request con información del dispositivo
            String deviceInfo = android.os.Build.MODEL + " (" + android.os.Build.VERSION.RELEASE + ")";
            FCMApiService.FCMTokenRequest request = new FCMApiService.FCMTokenRequest(userId, token, deviceInfo);
            
            // Enviar token al servidor
            apiClient.getFCMApiService().registerFCMToken("Bearer " + jwtToken, request)
                    .enqueue(new retrofit2.Callback<String>() {
                        @Override
                        public void onResponse(retrofit2.Call<String> call, retrofit2.Response<String> response) {
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Token FCM enviado exitosamente al servidor para usuario: " + userId);
                            } else {
                                Log.e(TAG, "Error enviando token FCM al servidor: " + response.code());
                            }
                        }
                        
                        @Override
                        public void onFailure(retrofit2.Call<String> call, Throwable t) {
                            Log.e(TAG, "Error de conexión enviando token FCM: " + t.getMessage());
                        }
                    });
            
        } else {
            Log.w(TAG, "No se puede enviar token al servidor - usuario no autenticado");
        }
    }
    
    private void saveTokenLocally(String token) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().putString("FCM_TOKEN", token).apply();
        Log.d(TAG, "Token FCM guardado localmente");
    }
} 
*/ 