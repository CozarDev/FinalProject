package com.proyectofinal.frontend.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
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
import com.proyectofinal.frontend.Activities.MainActivity;
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
                    
                    // Si el departmentId es el del departamento de incidencias, configurar directamente
                    // ID conocido del departamento de incidencias: 6830c77cdc74487c0c3436f6
                    if ("6830c77cdc74487c0c3436f6".equals(currentDepartmentId)) {
                        currentDepartmentName = "Incidencias";
                        checkIfIncidencesDepartmentEmployee();
                        setupTabsBasedOnRole();
                        setupFloatingActionButton();
                        isDepartmentInfoLoaded = true;
                        Log.d(TAG, "Empleado del departamento de incidencias detectado directamente");
                    } else {
                        // Intentar obtener nombre del departamento
                        loadDepartmentInfo(employee.getDepartmentId());
                    }
                    
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
        
        // Comentado el toast molesto - es normal que algunos usuarios no tengan departamento
        // Toast.makeText(requireContext(), 
        //               "No se pudo cargar información del departamento", 
        //               Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Configurado como empleado sin departamento específico");
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
        
        // Para empleados del departamento de incidencias, verificar dinámicamente las pestañas
        if (isIncidencesDepartmentEmployee) {
            checkAndUpdateIncidenceEmployeeTabs();
        }
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

    // Empleados departamento Incidencias: Comportamiento dinámico según asignaciones
    private void setupIncidenceEmployeeTabs(List<IncidencesPagerAdapter.TabInfo> tabs) {
        // Por defecto, mostrar solo pendientes
        // La lógica dinámica se manejará después de crear el adaptador
        tabs.add(new IncidencesPagerAdapter.TabInfo("⏳ Pendientes", IncidencesPagerAdapter.TabType.PENDING_INCIDENCES));
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
        try {
            Log.d(TAG, "Iniciando actualización de todas las pestañas");
            
            if (pagerAdapter != null) {
                pagerAdapter.refreshAllTabs();
                Log.d(TAG, "Pestañas actualizadas exitosamente");
            } else {
                Log.w(TAG, "pagerAdapter es null, no se pueden actualizar las pestañas");
            }
            
            // Para empleados del departamento de incidencias, también verificar pestañas dinámicas
            if (isIncidencesDepartmentEmployee && isDepartmentInfoLoaded) {
                Log.d(TAG, "Verificando pestañas dinámicas para empleado de incidencias");
                // Usar un pequeño delay para evitar conflictos
                new Handler().postDelayed(() -> {
                    try {
                        checkAndUpdateIncidenceEmployeeTabs();
                    } catch (Exception e) {
                        Log.e(TAG, "Error verificando pestañas dinámicas", e);
                    }
                }, 500);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error al refrescar pestañas", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error al actualizar. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método público para actualizar una incidencia desde otros fragmentos
    public void updateIncidence(Incidence updatedIncidence) {
        if (pagerAdapter != null) {
            pagerAdapter.notifyIncidenceUpdated(updatedIncidence);
        }
        
        // Si es empleado del departamento de incidencias y la incidencia está EN_CURSO y asignada a él
        if (isIncidencesDepartmentEmployee && 
            updatedIncidence.getStatus() == Incidence.Status.EN_CURSO && 
            currentUserId.equals(updatedIncidence.getAssignedTo())) {
            
            // Mostrar los detalles de la incidencia aceptada
            showIncidenceDetail(updatedIncidence.getId());
        } else if (isIncidencesDepartmentEmployee) {
            // Para otros casos, refrescar pestañas dinámicamente
            refreshTabsForIncidenceEmployee();
        }
    }
    
    // Método para mostrar los detalles de una incidencia
    private void showIncidenceDetail(String incidenceId) {
        Log.d(TAG, "Mostrando detalles de incidencia: " + incidenceId);
        
        IncidenceDetailFragment detailFragment = IncidenceDetailFragment.newInstance(incidenceId);
        
        // Configurar listener para cuando se resuelva la incidencia
        detailFragment.setOnIncidenceResolvedListener(() -> {
            Log.d(TAG, "Incidencia resuelta, volviendo a vista de pendientes");
            // Refrescar las pestañas para volver a mostrar pendientes
            refreshTabsForIncidenceEmployee();
        });
        
        // Navegar al fragmento de detalles
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).replaceFragment(detailFragment);
        }
    }
    
    // Método para verificar y actualizar pestañas dinámicamente para empleados de incidencias
    private void checkAndUpdateIncidenceEmployeeTabs() {
        if (!isIncidencesDepartmentEmployee || pagerAdapter == null) {
            return;
        }
        
        // Obtener incidencias asignadas al empleado actual
        incidenceApiService.getAssignedIncidences(new IncidenceApiService.IncidenceListCallback() {
            @Override
            public void onSuccess(List<com.proyectofinal.frontend.Models.Incidence> assignedIncidences) {
                // Filtrar incidencias en progreso
                List<com.proyectofinal.frontend.Models.Incidence> inProgressIncidences = assignedIncidences.stream()
                        .filter(incidence -> "EN_PROCESO".equals(incidence.getStatus()))
                        .collect(java.util.stream.Collectors.toList());
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        List<IncidencesPagerAdapter.TabInfo> newTabs = new ArrayList<>();
                        
                        if (inProgressIncidences.isEmpty()) {
                            // No tiene incidencias asignadas en progreso - mostrar solo pendientes
                            newTabs.add(new IncidencesPagerAdapter.TabInfo("⏳ Pendientes", IncidencesPagerAdapter.TabType.PENDING_INCIDENCES));
                        } else {
                            // Tiene incidencias en progreso - mostrar solo sus asignadas
                            newTabs.add(new IncidencesPagerAdapter.TabInfo("🔧 Mis Asignadas", IncidencesPagerAdapter.TabType.MY_ASSIGNED_INCIDENCES));
                        }
                        
                        // Actualizar el adaptador solo si las pestañas han cambiado
                        updateTabsIfChanged(newTabs);
                    });
                }
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error obteniendo incidencias asignadas: " + error);
                // En caso de error, mantener las pestañas actuales
            }
        });
    }
    
    // Método para actualizar pestañas solo si han cambiado
    private void updateTabsIfChanged(List<IncidencesPagerAdapter.TabInfo> newTabs) {
        if (pagerAdapter == null || newTabs.isEmpty()) {
            return;
        }
        
        // Verificar si las pestañas han cambiado
        boolean tabsChanged = false;
        if (pagerAdapter.getItemCount() != newTabs.size()) {
            tabsChanged = true;
        } else {
            // Comparar títulos de pestañas
            for (int i = 0; i < newTabs.size(); i++) {
                String currentTabTitle = pagerAdapter.getTabTitle(i);
                String newTabTitle = newTabs.get(i).getTitle();
                if (!newTabTitle.equals(currentTabTitle)) {
                    tabsChanged = true;
                    break;
                }
            }
        }
        
        // Solo actualizar si las pestañas han cambiado
        if (tabsChanged) {
            Log.d(TAG, "Actualizando pestañas dinámicamente para empleado de incidencias");
            
            try {
                // Actualizar el adaptador con las nuevas pestañas
                pagerAdapter.updateTabs(newTabs);
                
                // Reconfigurar TabLayout con ViewPager2
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                    if (position < newTabs.size()) {
                        tab.setText(newTabs.get(position).getTitle());
                    }
                }).attach();
                
                Log.d(TAG, "Pestañas actualizadas exitosamente");
            } catch (Exception e) {
                Log.e(TAG, "Error actualizando pestañas dinámicamente", e);
            }
        } else {
            Log.d(TAG, "Las pestañas no han cambiado, no es necesario actualizar");
        }
    }
    
    // Método para refrescar las pestañas dinámicamente para empleados de incidencias
    private void refreshTabsForIncidenceEmployee() {
        checkAndUpdateIncidenceEmployeeTabs();
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

    // Método para ser llamado cuando se selecciona esta pestaña
    public void onPageSelected() {
        Log.d(TAG, "onPageSelected() - IncidencesFragment seleccionado");
        
        // Refrescar todas las pestañas cuando se navega a esta sección
        if (getView() != null && isAdded() && !isDetached() && isDepartmentInfoLoaded) {
            Log.d(TAG, "Refrescando todas las pestañas de incidencias");
            refreshAllTabs();
        } else if (!isDepartmentInfoLoaded) {
            Log.d(TAG, "Información del departamento aún no cargada, esperando...");
            // Si la información del departamento no está cargada, intentar cargarla de nuevo
            loadEmployeeInfoAndSetupTabs();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - IncidencesFragment visible");
        
        // Actualizar datos cuando el fragmento vuelve a ser visible
        if (getView() != null && isAdded() && isDepartmentInfoLoaded) {
            Log.d(TAG, "Refrescando datos en onResume");
            refreshAllTabs();
        }
    }
} 