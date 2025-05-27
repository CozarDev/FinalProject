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
        
        // Actualizar datos cuando el fragmento vuelve a ser visible
        if (getView() != null && isAdded() && isDepartmentInfoLoaded) {
            Log.d(TAG, "Refrescando datos en onResume");
            refreshAllTabs();
        }
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
        Log.d(TAG, "refreshCurrentTab() - refrescando pestaña actual");
        
        if (pagerAdapter != null && viewPager != null) {
            int currentPosition = viewPager.getCurrentItem();
            Log.d(TAG, "Refrescando fragmento en posición: " + currentPosition);
            pagerAdapter.refreshFragment(currentPosition);
        } else {
            Log.w(TAG, "pagerAdapter o viewPager es null");
        }
    }

    public void refreshAllTabs() {
        Log.d(TAG, "refreshAllTabs() - refrescando todas las pestañas");
        refreshAllTabsDirectly();
    }
    
    private void refreshAllTabsDirectly() {
        Log.d(TAG, "refreshAllTabsDirectly() - método directo de refresh");
        
        if (pagerAdapter != null) {
            Log.d(TAG, "Usando método del adaptador para refrescar fragmentos");
            pagerAdapter.refreshAllWorkReportFragments();
        } else {
            Log.w(TAG, "pagerAdapter es null, no se pueden refrescar los fragmentos");
        }
    }
    
    // Método específico para refrescar después de crear un parte de trabajo
    public void refreshAfterWorkReportCreated() {
        Log.d(TAG, "refreshAfterWorkReportCreated() - forzando refresh inmediato");
        
        // Forzar refresh inmediato después de crear un parte de trabajo
        if (getView() != null && isAdded() && !isDetached()) {
            new Handler().postDelayed(() -> {
                if (isAdded() && !isDetached()) {
                    Log.d(TAG, "Ejecutando refresh forzado después de crear parte");
                    refreshAllTabsDirectly();
                }
            }, 500); // Delay más largo para asegurar que la creación se completó
        }
    }
    
    // Método público para forzar refresh desde MainActivity
    public void forceRefresh() {
        Log.d(TAG, "forceRefresh() - refresh forzado desde MainActivity");
        
        if (getView() != null && isAdded() && !isDetached()) {
            refreshAllTabsDirectly();
        }
    }
    
    // Método para ser llamado cuando se selecciona esta pestaña
    public void onPageSelected() {
        Log.d(TAG, "onPageSelected() - WorkReportsFragment seleccionado");
        
        // Refrescar todas las pestañas cuando se navega a esta sección
        if (getView() != null && isAdded() && !isDetached() && isDepartmentInfoLoaded) {
            Log.d(TAG, "Refrescando todas las pestañas de work reports");
            
            // Usar un pequeño delay para asegurar que todo esté listo
            new Handler().postDelayed(() -> {
                if (isAdded() && !isDetached()) {
                    refreshAllTabsDirectly();
                }
            }, 100);
        } else if (!isDepartmentInfoLoaded) {
            Log.d(TAG, "Información del departamento aún no cargada, esperando...");
            // Si la información del departamento no está cargada, intentar cargarla de nuevo
            setupTabsBasedOnRole();
        }
    }

}