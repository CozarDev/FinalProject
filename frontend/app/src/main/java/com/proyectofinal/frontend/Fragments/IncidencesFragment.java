package com.proyectofinal.frontend.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.proyectofinal.frontend.Adapters.IncidenceAdapter;
import com.proyectofinal.frontend.Adapters.IncidencesPagerAdapter;
import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.Models.Incidence;
import com.proyectofinal.frontend.R;
import com.proyectofinal.frontend.Services.IncidenceApiService;
import com.proyectofinal.frontend.Api.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncidencesFragment extends Fragment {

    private static final String TAG = "IncidencesFragment";
    private static final String INCIDENCES_DEPARTMENT_NAME = "Incidencias";
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FloatingActionButton fabAdd;
    
    private IncidencesPagerAdapter pagerAdapter;
    private IncidenceApiService incidenceApiService;
    private ApiClient apiClient;
    
    private String userRole;
    private String currentUserId;
    private String currentDepartmentId;
    private String currentDepartmentName;
    private boolean isIncidencesDepartmentEmployee = false;
    private boolean isIncidencesDepartmentManager = false;
    private boolean isDepartmentInfoLoaded = false;
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Obtener información del usuario desde SharedPreferences
        SharedPreferences sharedPrefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        userRole = sharedPrefs.getString("USER_ROLE", "EMPLOYEE");
        currentUserId = sharedPrefs.getString("USER_ID", "");
        
        // Inicializar API Service
        apiClient = new ApiClient(requireContext());
        incidenceApiService = new IncidenceApiService(apiClient);
        
        Log.d(TAG, "Usuario actual - Role: " + userRole + ", ID: " + currentUserId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_incidences, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        Log.d(TAG, "onViewCreated iniciado");
        
        try {
            initializeViews(view);
            Log.d(TAG, "Views inicializadas correctamente");
            
            // Verificar departamento del empleado desde el backend
            loadEmployeeInfoAndSetupTabs();
            
            Log.d(TAG, "onViewCreated completado exitosamente");
        
        // DEBUG TEMPORAL: Probar autenticación
        testDebugAuth();
        } catch (Exception e) {
            Log.e(TAG, "Error en onViewCreated", e);
            throw e;
        }
    }

    private void initializeViews(View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        fabAdd = view.findViewById(R.id.fabAdd);
        
        // Inicializar botón de actualizar
        View btnRefresh = view.findViewById(R.id.btnRefresh);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                // Mostrar feedback visual al usuario
                Toast.makeText(requireContext(), "Actualizando todas las pestañas...", Toast.LENGTH_SHORT).show();
                refreshAllTabs();
            });
        }
    }

    private void loadEmployeeInfoAndSetupTabs() {
        // Si es admin, configurar directamente las pestañas
        if ("ADMIN".equals(userRole)) {
            isIncidencesDepartmentEmployee = false;
            isIncidencesDepartmentManager = false;
            currentDepartmentName = "Administración";
            isDepartmentInfoLoaded = true;
            setupTabsBasedOnRole();
            setupFloatingActionButton();
            return;
        }

        // Para empleados y jefes de departamento, obtener información del backend
        apiClient.getEmployeeApiService().getCurrentEmployeeInfo().enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Employee employee = response.body();
                    currentDepartmentId = employee.getDepartmentId();
                    
                    // Obtener nombre del departamento
                    loadDepartmentInfo(employee.getDepartmentId());
                    
                } else {
                    Log.e(TAG, "Error obteniendo información del empleado: " + response.code());
                    handleEmployeeInfoError();
                }
            }

            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                Log.e(TAG, "Error de conexión obteniendo empleado", t);
                handleEmployeeInfoError();
            }
        });
    }

    private void loadDepartmentInfo(String departmentId) {
        if (departmentId == null || departmentId.isEmpty()) {
            handleEmployeeInfoError();
            return;
        }

        apiClient.getDepartmentApiService().getDepartmentById(departmentId).enqueue(new Callback<com.proyectofinal.frontend.Models.Department>() {
            @Override
            public void onResponse(Call<com.proyectofinal.frontend.Models.Department> call, 
                                 Response<com.proyectofinal.frontend.Models.Department> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentDepartmentName = response.body().getName();
                    
                    // Verificar si pertenece al departamento de incidencias
                    checkIfIncidencesDepartmentEmployee();
                    
                    // Configurar pestañas y UI
                    setupTabsBasedOnRole();
                    setupFloatingActionButton();
                    
                    isDepartmentInfoLoaded = true;
                    Log.d(TAG, "Información del departamento cargada: " + currentDepartmentName);
                    
                } else {
                    Log.e(TAG, "Error obteniendo departamento: " + response.code());
                    handleEmployeeInfoError();
                }
            }

            @Override
            public void onFailure(Call<com.proyectofinal.frontend.Models.Department> call, Throwable t) {
                Log.e(TAG, "Error de conexión obteniendo departamento", t);
                handleEmployeeInfoError();
            }
        });
    }

    private void handleEmployeeInfoError() {
        // En caso de error, configurar como empleado regular sin departamento de incidencias
        isIncidencesDepartmentEmployee = false;
        isIncidencesDepartmentManager = false;
        currentDepartmentName = "Sin departamento";
        isDepartmentInfoLoaded = true;
        
        setupTabsBasedOnRole();
        setupFloatingActionButton();
        
        Toast.makeText(requireContext(), 
                      "No se pudo cargar información del departamento", 
                      Toast.LENGTH_SHORT).show();
    }

    private void setupTabsBasedOnRole() {
        List<IncidencesPagerAdapter.TabInfo> tabs = new ArrayList<>();
        
        // Determinar qué pestañas mostrar según el rol y departamento
        if ("ADMIN".equals(userRole)) {
            setupAdminTabs(tabs);
        } else if (isIncidencesDepartmentManager) {
            setupIncidenceManagerTabs(tabs);
        } else if (isIncidencesDepartmentEmployee) {
            setupIncidenceEmployeeTabs(tabs);
        } else if ("DEPARTMENT_HEAD".equals(userRole)) {
            setupOtherDepartmentHeadTabs(tabs);
        } else {
            setupOtherEmployeeTabs(tabs);
        }
        
        // Configurar ViewPager y Tabs
        pagerAdapter = new IncidencesPagerAdapter(this, tabs, getUserRole());
        viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabs.get(position).getTitle())
        ).attach();
        
        Log.d(TAG, "Configuradas " + tabs.size() + " pestañas para rol: " + userRole + 
                  " en departamento: " + currentDepartmentName);
    }

    // ADMIN: Todas las funcionalidades + crear y eliminar cualquier incidencia
    private void setupAdminTabs(List<IncidencesPagerAdapter.TabInfo> tabs) {
        tabs.add(new IncidencesPagerAdapter.TabInfo("⏳ Pendientes", IncidencesPagerAdapter.TabType.PENDING_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("🔧 En Progreso", IncidencesPagerAdapter.TabType.IN_PROGRESS_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("✅ Resueltas", IncidencesPagerAdapter.TabType.RESOLVED_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("📊 Estadísticas", IncidencesPagerAdapter.TabType.STATS));
    }

    // Jefe departamento Incidencias: Todo lo anterior + ver activas, en curso con asignaciones, y resueltas
    private void setupIncidenceManagerTabs(List<IncidencesPagerAdapter.TabInfo> tabs) {
        tabs.add(new IncidencesPagerAdapter.TabInfo("⏳ Pendientes", IncidencesPagerAdapter.TabType.PENDING_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("🔧 En Progreso", IncidencesPagerAdapter.TabType.IN_PROGRESS_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("✅ Resueltas", IncidencesPagerAdapter.TabType.RESOLVED_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("📊 Estadísticas", IncidencesPagerAdapter.TabType.STATS));
    }

    // Empleados departamento Incidencias: Ver pendientes ordenadas por prioridad, aceptar incidencias, resolver asignadas
    private void setupIncidenceEmployeeTabs(List<IncidencesPagerAdapter.TabInfo> tabs) {
        tabs.add(new IncidencesPagerAdapter.TabInfo("⏳ Pendientes", IncidencesPagerAdapter.TabType.PENDING_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("🔧 Mis Asignadas", IncidencesPagerAdapter.TabType.MY_ASSIGNED_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("✅ Resueltas", IncidencesPagerAdapter.TabType.RESOLVED_INCIDENCES));
    }

    // Jefes otros departamentos: Crear incidencias, ver suyas y de empleados de su departamento, eliminar pendientes
    private void setupOtherDepartmentHeadTabs(List<IncidencesPagerAdapter.TabInfo> tabs) {
        tabs.add(new IncidencesPagerAdapter.TabInfo("📝 Mis Incidencias", IncidencesPagerAdapter.TabType.MY_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("🏢 Departamento", IncidencesPagerAdapter.TabType.DEPARTMENT_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("⏳ Pendientes Dept.", IncidencesPagerAdapter.TabType.PENDING_DEPT));
        tabs.add(new IncidencesPagerAdapter.TabInfo("✅ Resueltas Dept.", IncidencesPagerAdapter.TabType.RESOLVED_DEPT));
    }

    // Empleados otros departamentos: Crear propias, ver solo suyas, eliminar solo pendientes propias
    private void setupOtherEmployeeTabs(List<IncidencesPagerAdapter.TabInfo> tabs) {
        tabs.add(new IncidencesPagerAdapter.TabInfo("📝 Mis Incidencias", IncidencesPagerAdapter.TabType.MY_INCIDENCES));
        tabs.add(new IncidencesPagerAdapter.TabInfo("⏳ Mis Pendientes", IncidencesPagerAdapter.TabType.MY_PENDING));
        tabs.add(new IncidencesPagerAdapter.TabInfo("🔧 En Progreso", IncidencesPagerAdapter.TabType.MY_IN_PROGRESS));
        tabs.add(new IncidencesPagerAdapter.TabInfo("✅ Mis Resueltas", IncidencesPagerAdapter.TabType.MY_RESOLVED));
    }

    private void setupFloatingActionButton() {
        // Determinar quién puede crear incidencias
        boolean canCreateIncidences = canCurrentUserCreateIncidences();
        
        if (canCreateIncidences) {
            fabAdd.setVisibility(View.VISIBLE);
            fabAdd.setOnClickListener(v -> openCreateIncidenceDialog());
        } else {
            fabAdd.setVisibility(View.GONE);
        }
        
        Log.d(TAG, "FAB configurado - Puede crear incidencias: " + canCreateIncidences);
    }

    private boolean canCurrentUserCreateIncidences() {
        // ADMIN puede crear cualquier incidencia
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // Jefes de departamento (tanto de incidencias como otros) pueden crear incidencias
        if ("DEPARTMENT_HEAD".equals(userRole)) {
            return true;
        }
        
        // Empleados de otros departamentos (NO incidencias) pueden crear sus propias incidencias
        if ("EMPLOYEE".equals(userRole) && !isIncidencesDepartmentEmployee) {
            return true;
        }
        
        // Empleados del departamento de incidencias NO pueden crear incidencias (solo resolver)
        return false;
    }

    private void openCreateIncidenceDialog() {
        CreateIncidenceDialogFragment dialog = CreateIncidenceDialogFragment.newInstance(
            "ADMIN".equals(userRole), 
            currentUserId
        );
        
        dialog.setOnIncidenceCreatedListener(this::onIncidenceCreated);
        dialog.show(getChildFragmentManager(), "CreateIncidenceDialog");
    }

    private void onIncidenceCreated(Incidence newIncidence) {
        // Notificar a los fragmentos que se ha creado una nueva incidencia
        if (pagerAdapter != null) {
            pagerAdapter.notifyIncidenceCreated(newIncidence);
        }
        
        Toast.makeText(requireContext(), "Incidencia creada exitosamente", Toast.LENGTH_SHORT).show();
    }

    private void checkIfIncidencesDepartmentEmployee() {
        if (currentDepartmentName == null) {
            isIncidencesDepartmentEmployee = false;
            isIncidencesDepartmentManager = false;
            return;
        }
        
        // Verificar si el empleado pertenece al departamento de "Incidencias"
        boolean isIncidencesDepartment = INCIDENCES_DEPARTMENT_NAME.equalsIgnoreCase(currentDepartmentName);
        
        isIncidencesDepartmentEmployee = isIncidencesDepartment && "EMPLOYEE".equals(userRole);
        isIncidencesDepartmentManager = isIncidencesDepartment && "DEPARTMENT_HEAD".equals(userRole);
        
        Log.d(TAG, "Departamento: " + currentDepartmentName + 
                  ", Es empleado de incidencias: " + isIncidencesDepartmentEmployee + 
                  ", Es jefe de incidencias: " + isIncidencesDepartmentManager);
    }

    private IncidenceAdapter.UserRole getUserRole() {
        if ("ADMIN".equals(userRole)) {
            return IncidenceAdapter.UserRole.ADMIN;
        } else if (isIncidencesDepartmentManager) {
            return IncidenceAdapter.UserRole.INCIDENCE_MANAGER;
        } else if (isIncidencesDepartmentEmployee) {
            return IncidenceAdapter.UserRole.INCIDENCE_EMPLOYEE;
        } else if ("DEPARTMENT_HEAD".equals(userRole)) {
            return IncidenceAdapter.UserRole.DEPARTMENT_HEAD;
        } else {
            return IncidenceAdapter.UserRole.EMPLOYEE;
        }
    }

    public void refreshCurrentTab() {
        if (pagerAdapter != null && viewPager != null) {
            int currentPosition = viewPager.getCurrentItem();
            pagerAdapter.refreshTab(currentPosition);
        }
    }

    public void refreshAllTabs() {
        if (pagerAdapter != null) {
            pagerAdapter.refreshAllTabs();
        }
    }

    // Método público para actualizar una incidencia desde otros fragmentos
    public void updateIncidence(Incidence updatedIncidence) {
        if (pagerAdapter != null) {
            pagerAdapter.notifyIncidenceUpdated(updatedIncidence);
        }
    }

    // Método público para eliminar una incidencia desde otros fragmentos
    public void removeIncidence(Incidence incidence) {
        if (pagerAdapter != null) {
            pagerAdapter.notifyIncidenceRemoved(incidence);
        }
    }
    
    // **MÉTODO TEMPORAL PARA DEBUG**
    private void testDebugAuth() {
        Log.d(TAG, "Probando debug auth...");
        
        apiClient.get("api/incidences/debug-auth", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "DEBUG AUTH SUCCESS: " + response);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "DEBUG AUTH ERROR: " + error);
            }
        });
    }
} 