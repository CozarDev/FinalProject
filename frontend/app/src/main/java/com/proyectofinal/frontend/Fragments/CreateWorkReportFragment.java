package com.proyectofinal.frontend.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.WorkReportApiService;
import com.proyectofinal.frontend.Models.WorkReport;
import com.proyectofinal.frontend.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateWorkReportFragment extends Fragment {
    
    private EditText etReportDate;
    private EditText etStartTime;
    private EditText etEndTime;
    private EditText etBreakDuration;
    private EditText etObservations;
    private Button btnSaveReport;
    private Button btnCancel;
    
    private WorkReportApiService apiService;
    private Handler mainHandler;
    
    private LocalDate selectedDate;
    private String selectedStartTime;
    private String selectedEndTime;
    
    private OnWorkReportCreatedListener listener;
    
    public interface OnWorkReportCreatedListener {
        void onWorkReportCreated();
        void onCancel();
    }
    
    public void setOnWorkReportCreatedListener(OnWorkReportCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Establecer fecha actual por defecto
        selectedDate = LocalDate.now();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_work_report, container, false);
        
        initializeViews(view);
        setupClickListeners();
        updateDateDisplay();
        
        // Inicializar ApiService después de que el contexto esté disponible
        ApiClient.getInstance(getContext());
        apiService = ApiClient.getClient().create(WorkReportApiService.class);
        
        return view;
    }
    
    private void initializeViews(View view) {
        etReportDate = view.findViewById(R.id.etReportDate);
        etStartTime = view.findViewById(R.id.etStartTime);
        etEndTime = view.findViewById(R.id.etEndTime);
        etBreakDuration = view.findViewById(R.id.etBreakDuration);
        etObservations = view.findViewById(R.id.etObservations);
        btnSaveReport = view.findViewById(R.id.btnSaveReport);
        btnCancel = view.findViewById(R.id.btnCancel);
        
        // Hacer los campos de fecha y hora no editables directamente
        etReportDate.setFocusable(false);
        etReportDate.setClickable(true);
        etStartTime.setFocusable(false);
        etStartTime.setClickable(true);
        etEndTime.setFocusable(false);
        etEndTime.setClickable(true);
    }
    
    private void setupClickListeners() {
        etReportDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndTime.setOnClickListener(v -> showTimePicker(false));
        btnSaveReport.setOnClickListener(v -> saveWorkReport());
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel();
            }
        });
    }
    
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDate != null) {
            calendar.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
        }
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth);
                    updateDateDisplay();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }
    
    private void showTimePicker(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minute);
                    if (isStartTime) {
                        selectedStartTime = time;
                        etStartTime.setText(time);
                    } else {
                        selectedEndTime = time;
                        etEndTime.setText(time);
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        
        timePickerDialog.show();
    }
    
    private void updateDateDisplay() {
        if (selectedDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            etReportDate.setText(selectedDate.format(formatter));
        }
    }
    
    private void saveWorkReport() {
        android.util.Log.d("CreateWorkReportFragment", "saveWorkReport() iniciado");
        
        if (!validateForm()) {
            android.util.Log.w("CreateWorkReportFragment", "Validación del formulario falló");
            return;
        }
        
        android.util.Log.d("CreateWorkReportFragment", "Formulario validado correctamente");
        
        // Deshabilitar botón mientras se guarda
        btnSaveReport.setEnabled(false);
        btnSaveReport.setText("Guardando...");
        
        try {
            Map<String, Object> workReportData = new HashMap<>();
            workReportData.put("reportDate", selectedDate.toString());
            workReportData.put("startTime", selectedStartTime);
            workReportData.put("endTime", selectedEndTime);
            workReportData.put("breakDuration", Integer.parseInt(etBreakDuration.getText().toString()));
            
            String observations = etObservations.getText().toString().trim();
            if (!observations.isEmpty()) {
                workReportData.put("observations", observations);
            }
            
            android.util.Log.d("CreateWorkReportFragment", "Datos del parte preparados: " + workReportData.toString());
            
            Call<WorkReport> call = apiService.createWorkReport(workReportData);
            android.util.Log.d("CreateWorkReportFragment", "Llamada API creada, ejecutando...");
            
            call.enqueue(new Callback<WorkReport>() {
                @Override
                public void onResponse(Call<WorkReport> call, Response<WorkReport> response) {
                    android.util.Log.d("CreateWorkReportFragment", "onResponse recibido - Código: " + response.code());
                    
                    try {
                        mainHandler.post(() -> {
                            try {
                                btnSaveReport.setEnabled(true);
                                btnSaveReport.setText("💾 Guardar Parte");
                                
                                if (response.isSuccessful()) {
                                    android.util.Log.d("CreateWorkReportFragment", "Respuesta exitosa - Parte guardado");
                                    Toast.makeText(getContext(), 
                                            "Parte de trabajo guardado correctamente", Toast.LENGTH_SHORT).show();
                                    if (listener != null) {
                                        android.util.Log.d("CreateWorkReportFragment", "Llamando listener.onWorkReportCreated()");
                                        listener.onWorkReportCreated();
                                    } else {
                                        android.util.Log.w("CreateWorkReportFragment", "Listener es null");
                                    }
                                } else {
                                    android.util.Log.e("CreateWorkReportFragment", "Error en respuesta: " + response.code());
                                    String errorMessage = "Error al guardar el parte";
                                    if (response.code() == 400) {
                                        errorMessage = "Ya existe un parte para esta fecha";
                                    }
                                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                android.util.Log.e("CreateWorkReportFragment", "Error en mainHandler.post", e);
                            }
                        });
                    } catch (Exception e) {
                        android.util.Log.e("CreateWorkReportFragment", "Error en onResponse", e);
                    }
                }

                @Override
                public void onFailure(Call<WorkReport> call, Throwable t) {
                    android.util.Log.e("CreateWorkReportFragment", "onFailure - Error: " + t.getMessage(), t);
                    
                    try {
                        mainHandler.post(() -> {
                            try {
                                btnSaveReport.setEnabled(true);
                                btnSaveReport.setText("💾 Guardar Parte");
                                Toast.makeText(getContext(), 
                                        "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            } catch (Exception e) {
                                android.util.Log.e("CreateWorkReportFragment", "Error en onFailure mainHandler.post", e);
                            }
                        });
                    } catch (Exception e) {
                        android.util.Log.e("CreateWorkReportFragment", "Error en onFailure", e);
                    }
                }
            });
            
        } catch (Exception e) {
            android.util.Log.e("CreateWorkReportFragment", "Error general en saveWorkReport", e);
            btnSaveReport.setEnabled(true);
            btnSaveReport.setText("💾 Guardar Parte");
            Toast.makeText(getContext(), "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private boolean validateForm() {
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Selecciona una fecha", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedStartTime == null || selectedStartTime.isEmpty()) {
            Toast.makeText(getContext(), "Selecciona la hora de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        if (selectedEndTime == null || selectedEndTime.isEmpty()) {
            Toast.makeText(getContext(), "Selecciona la hora de fin", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        String breakDurationStr = etBreakDuration.getText().toString().trim();
        if (breakDurationStr.isEmpty()) {
            Toast.makeText(getContext(), "Introduce la duración del descanso", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        try {
            int breakDuration = Integer.parseInt(breakDurationStr);
            if (breakDuration < 0 || breakDuration > 480) {
                Toast.makeText(getContext(), "La duración del descanso debe estar entre 0 y 480 minutos", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Introduce un número válido para el descanso", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        // Validar que la hora de fin sea posterior a la de inicio
        if (!isEndTimeAfterStartTime()) {
            Toast.makeText(getContext(), "La hora de fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show();
            return false;
        }
        
        return true;
    }
    
    private boolean isEndTimeAfterStartTime() {
        try {
            String[] startParts = selectedStartTime.split(":");
            String[] endParts = selectedEndTime.split(":");
            
            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
            
            return endMinutes > startMinutes;
        } catch (Exception e) {
            return false;
        }
    }
} 