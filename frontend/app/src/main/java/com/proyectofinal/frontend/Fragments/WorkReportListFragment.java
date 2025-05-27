package com.proyectofinal.frontend.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.proyectofinal.frontend.Adapters.WorkReportAdapter;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.WorkReportApiService;
import com.proyectofinal.frontend.Models.WorkReport;
import com.proyectofinal.frontend.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkReportListFragment extends Fragment {
    
    private static final String TAG = "WorkReportListFragment";
    
    private RecyclerView recyclerView;
    private WorkReportAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<WorkReport> workReports;
    private WorkReportApiService apiService;
    private Handler mainHandler;
    private boolean isUserVisible = false;
    private boolean hasLoadedData = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.util.Log.d(TAG, "onCreate() iniciado");
        ApiClient.getInstance(getContext());
        apiService = ApiClient.getClient().create(WorkReportApiService.class);
        mainHandler = new Handler(Looper.getMainLooper());
        workReports = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        android.util.Log.d(TAG, "onCreateView() iniciado");
        View view = inflater.inflate(R.layout.fragment_work_report_list, container, false);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        
        // Cargar datos iniciales
        loadWorkReports();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d(TAG, "onResume() - fragmento visible, refrescando datos");
        
        // Siempre refrescar cuando el fragmento vuelve a ser visible
        if (adapter != null && getView() != null) {
            // Usar un pequeño delay para asegurar que la UI esté lista
            new Handler().postDelayed(() -> {
                if (isAdded() && !isDetached() && !isHidden()) {
                    android.util.Log.d(TAG, "Ejecutando refresh automático en onResume");
                    loadWorkReports();
                }
            }, 300); // Aumentar el delay para dar más tiempo
        }
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        android.util.Log.d(TAG, "onHiddenChanged: hidden=" + hidden);
        
        // Si el fragmento deja de estar oculto (se vuelve visible)
        if (!hidden && getView() != null && adapter != null) {
            android.util.Log.d(TAG, "Fragmento ya no está oculto, refrescando datos");
            new Handler().postDelayed(() -> {
                if (isAdded() && !isDetached() && !isHidden()) {
                    android.util.Log.d(TAG, "Ejecutando refresh en onHiddenChanged");
                    loadWorkReports();
                }
            }, 300);
        }
    }
    
    // Método para ser llamado desde el fragmento padre cuando se navega a esta pestaña
    public void onPageSelected() {
        android.util.Log.d(TAG, "onPageSelected() - pestaña seleccionada");
        android.util.Log.d(TAG, "Estado del fragmento - isAdded: " + isAdded() + ", isDetached: " + isDetached() + ", getView: " + (getView() != null) + ", adapter: " + (adapter != null));
        
        if (getView() != null && adapter != null) {
            android.util.Log.d(TAG, "Condiciones cumplidas, programando refresh con delay");
            new Handler().postDelayed(() -> {
                if (isAdded() && !isDetached()) {
                    android.util.Log.d(TAG, "Ejecutando refresh en onPageSelected después del delay");
                    loadWorkReports();
                } else {
                    android.util.Log.w(TAG, "Fragmento ya no es válido después del delay");
                }
            }, 200);
        } else {
            android.util.Log.w(TAG, "Condiciones no cumplidas para refresh en onPageSelected");
        }
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewWorkReports);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshWorkReports);
    }

    private void setupRecyclerView() {
        adapter = new WorkReportAdapter(workReports, null); // Quitar funcionalidad de click
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadWorkReports);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorAccent
        );
    }

    public void loadWorkReports() {
        android.util.Log.d(TAG, "loadWorkReports() iniciado");
        android.util.Log.d(TAG, "Estado del fragmento en loadWorkReports - isAdded: " + isAdded() + ", isDetached: " + isDetached());
        
        // Verificar que el fragmento esté en un estado válido
        if (!isAdded() || isDetached()) {
            android.util.Log.w(TAG, "Fragmento no está en estado válido, cancelando carga");
            return;
        }
        
        android.util.Log.d(TAG, "Fragmento válido, iniciando carga de datos");
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
            android.util.Log.d(TAG, "SwipeRefreshLayout activado");
        } else {
            android.util.Log.w(TAG, "SwipeRefreshLayout es null");
        }
        
        Call<List<Map<String, Object>>> call = apiService.getWorkReports();
        android.util.Log.d(TAG, "Llamada API iniciada");
        call.enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                // Verificar que el fragmento siga siendo válido antes de actualizar la UI
                if (!isAdded() || isDetached()) {
                    android.util.Log.w(TAG, "Fragmento ya no es válido, ignorando respuesta");
                    return;
                }
                
                mainHandler.post(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    
                    if (response.isSuccessful() && response.body() != null) {
                        android.util.Log.d(TAG, "Respuesta exitosa, " + response.body().size() + " partes recibidos");
                        workReports.clear();
                        
                        for (Map<String, Object> reportData : response.body()) {
                            WorkReport workReport = mapToWorkReport(reportData);
                            workReports.add(workReport);
                        }
                        
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            hasLoadedData = true;
                            android.util.Log.d(TAG, "Adaptador actualizado con " + workReports.size() + " partes");
                        }
                    } else {
                        android.util.Log.e(TAG, "Error en respuesta: " + response.code());
                        showError("Error al cargar los partes de trabajo");
                    }
                });
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                // Verificar que el fragmento siga siendo válido
                if (!isAdded() || isDetached()) {
                    android.util.Log.w(TAG, "Fragmento ya no es válido, ignorando error");
                    return;
                }
                
                mainHandler.post(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    android.util.Log.e(TAG, "Error en llamada API: " + t.getMessage());
                    showError("Error de conexión: " + t.getMessage());
                });
            }
        });
    }

    private WorkReport mapToWorkReport(Map<String, Object> data) {
        WorkReport workReport = new WorkReport();
        
        workReport.setId((String) data.get("id"));
        workReport.setEmployeeId((String) data.get("employeeId"));
        workReport.setEmployeeName((String) data.get("employeeName"));
        workReport.setEmployeeEmail((String) data.get("employeeEmail"));
        
        // Parsear fecha
        String reportDateStr = (String) data.get("reportDate");
        if (reportDateStr != null) {
            workReport.setReportDate(reportDateStr);
        }
        
        workReport.setStartTime((String) data.get("startTime"));
        workReport.setEndTime((String) data.get("endTime"));
        
        // Parsear breakDuration
        Object breakDurationObj = data.get("breakDuration");
        if (breakDurationObj instanceof Number) {
            workReport.setBreakDuration(((Number) breakDurationObj).intValue());
        }
        
        workReport.setObservations((String) data.get("observations"));
        
        // Parsear fechas de creación y actualización
        String createdAtStr = (String) data.get("createdAt");
        if (createdAtStr != null) {
            workReport.setCreatedAt(createdAtStr);
        }
        
        String updatedAtStr = (String) data.get("updatedAt");
        if (updatedAtStr != null) {
            workReport.setUpdatedAt(updatedAtStr);
        }
        
        return workReport;
    }

    // Método eliminado - ya no se permite ver detalles de partes de trabajo

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void refreshData() {
        android.util.Log.d("WorkReportListFragment", "refreshData() llamado");
        loadWorkReports();
    }
} 