package com.proyectofinal.frontend.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.proyectofinal.frontend.Activities.MainActivity;
import com.proyectofinal.frontend.Adapters.WorkReportsPagerAdapter;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.R;

import java.util.ArrayList;
import java.util.List;

public class WorkReportsFragment extends Fragment {

    private static final String TAG = "WorkReportsFragment";
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAdd;
    private TextView userRoleInfo;
    
    private WorkReportsPagerAdapter pagerAdapter;
    private ApiClient apiClient;
    
    private String userRole;
    private String currentUserId;
    private boolean isDepartmentInfoLoaded = false;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtener información del usuario desde SharedPreferences
        SharedPreferences sharedPrefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userRole = sharedPrefs.getString("USER_ROLE", "EMPLOYEE");
        currentUserId = sharedPrefs.getString("USER_ID", "");
        
        // Inicializar API Client
        apiClient = ApiClient.getInstance(requireContext());
        
        Log.d(TAG, "Usuario actual - Role: " + userRole + ", ID: " + currentUserId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_work_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Log.d(TAG, "onViewCreated iniciado");
        
        try {
            initializeViews(view);
            Log.d(TAG, "Views inicializadas correctamente");
            
            setupTabsBasedOnRole();
            setupFloatingActionButton();
            
            isDepartmentInfoLoaded = true;
            Log.d(TAG, "onViewCreated completado exitosamente");
            
        } catch (Exception e) {
            Log.e(TAG, "Error en onViewCreated", e);
            throw e;
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - WorkReportsFragment visible");
        // El refresh automático ahora se maneja en WorkReportListFragment
        // No necesitamos hacer nada especial aquí
    }

    private void initializeViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        fabAdd = view.findViewById(R.id.fabAdd);
        userRoleInfo = view.findViewById(R.id.userRoleInfo);
        
        // Actualizar información del rol
        updateUserRoleInfo();
        
        // Inicializar botón de actualizar
        View btnRefresh = view.findViewById(R.id.btnRefresh);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Actualizando partes de trabajo...", Toast.LENGTH_SHORT).show();
                refreshAllTabs();
            });
        }
    }

    private void updateUserRoleInfo() {
        String roleText;
        switch (userRole) {
            case "ADMIN":
                roleText = "Administrador";
                break;
            case "DEPARTMENT_HEAD":
                roleText = "Jefe de Departamento";
                break;
            default:
                roleText = "Empleado";
                break;
        }
        
        if (userRoleInfo != null) {
            userRoleInfo.setText(roleText);
        }
    }

    private void setupTabsBasedOnRole() {
        List<WorkReportsPagerAdapter.TabInfo> tabs = new ArrayList<>();
        
        // Todos los roles pueden ver la lista de partes
        tabs.add(new WorkReportsPagerAdapter.TabInfo("📋 Mis Partes", "LIST"));
        
        // Solo mostrar pestaña de crear si no usamos FAB
        // tabs.add(new WorkReportsPagerAdapter.TabInfo("➕ Crear Parte", "CREATE"));
        
        // Configurar adaptador
        pagerAdapter = new WorkReportsPagerAdapter(requireActivity());
        pagerAdapter.setTabs(tabs);
        viewPager.setAdapter(pagerAdapter);
        
        // Configurar TabLayout con ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(pagerAdapter.getTabTitle(position));
        }).attach();
        
        Log.d(TAG, "Pestañas configuradas para rol: " + userRole);
    }

    private void setupFloatingActionButton() {
        // Todos los usuarios pueden crear partes de trabajo
        fabAdd.setVisibility(View.VISIBLE);
        fabAdd.setOnClickListener(v -> openCreateWorkReportFragment());
        
        Log.d(TAG, "FAB configurado para crear partes");
    }

    private void openCreateWorkReportFragment() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.showCreateWorkReportFragment();
        }
        
        Log.d(TAG, "Abriendo fragmento de crear parte");
    }
    


    public void refreshCurrentTab() {
        Log.d(TAG, "refreshCurrentTab() - método simplificado");
        // El refresh automático se maneja en WorkReportListFragment cuando se vuelve visible
        // Solo loguear para depuración
    }

    public void refreshAllTabs() {
        Log.d(TAG, "refreshAllTabs() - método simplificado");
        // El refresh automático se maneja en WorkReportListFragment cuando se vuelve visible
        // Solo loguear para depuración
    }
    
    // Método específico para refrescar después de crear un parte de trabajo
    public void refreshAfterWorkReportCreated() {
        Log.d(TAG, "refreshAfterWorkReportCreated() - confiando en refresh automático");
        // El WorkReportListFragment se refrescará automáticamente cuando se vuelva visible
        // No necesitamos forzar nada aquí para evitar IllegalStateException
    }
    
    // Método para ser llamado cuando se selecciona esta pestaña
    public void onPageSelected() {
        Log.d(TAG, "onPageSelected() - WorkReportsFragment seleccionado");
        
        // Intentar notificar inmediatamente
        if (tryNotifyWorkReportListFragment()) {
            Log.d(TAG, "WorkReportListFragment notificado inmediatamente");
            return;
        }
        
        // Si no se encuentra, usar reintentos con delays
        Log.d(TAG, "WorkReportListFragment no encontrado, iniciando reintentos");
        retryNotifyWorkReportListFragment(0);
    }
    
    private boolean tryNotifyWorkReportListFragment() {
        if (pagerAdapter != null && viewPager != null) {
            int currentPosition = viewPager.getCurrentItem();
            Fragment currentFragment = getChildFragmentManager().findFragmentByTag("f" + currentPosition);
            
            Log.d(TAG, "Buscando fragmento en posición " + currentPosition + ": " + 
                  (currentFragment != null ? currentFragment.getClass().getSimpleName() : "null"));
            
            if (currentFragment instanceof WorkReportListFragment) {
                Log.d(TAG, "WorkReportListFragment encontrado, llamando onPageSelected");
                ((WorkReportListFragment) currentFragment).onPageSelected();
                return true;
            }
        }
        return false;
    }
    
    private void retryNotifyWorkReportListFragment(int attempt) {
        if (attempt >= 5) {
            Log.w(TAG, "Se agotaron los intentos para notificar WorkReportListFragment");
            return;
        }
        
        Log.d(TAG, "Intento #" + (attempt + 1) + " para notificar WorkReportListFragment");
        
        // Intentar notificar
        if (tryNotifyWorkReportListFragment()) {
            Log.d(TAG, "WorkReportListFragment notificado en intento #" + (attempt + 1));
            return;
        }
        
        // Si no se encuentra, reintentar con delay incremental
        int delay = 200 + (attempt * 200); // 200ms, 400ms, 600ms, 800ms, 1000ms
        new Handler().postDelayed(() -> {
            if (isAdded() && !isDetached()) {
                retryNotifyWorkReportListFragment(attempt + 1);
            }
        }, delay);
    }
}