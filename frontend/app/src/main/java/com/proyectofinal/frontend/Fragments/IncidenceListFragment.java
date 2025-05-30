package com.proyectofinal.frontend.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.proyectofinal.frontend.Adapters.IncidenceAdapter;
import com.proyectofinal.frontend.Adapters.IncidencesPagerAdapter;
import com.proyectofinal.frontend.Models.Incidence;
import com.proyectofinal.frontend.R;
import com.proyectofinal.frontend.Services.IncidenceApiService;
import com.proyectofinal.frontend.Api.ApiClient;

import java.util.ArrayList;
import java.util.List;

public class IncidenceListFragment extends Fragment implements IncidenceAdapter.OnIncidenceActionListener {

    private static final String TAG = "IncidenceListFragment";
    private static final String ARG_TAB_TYPE = "tab_type";
    private static final String ARG_USER_ROLE = "user_role";
    private static final String ARG_SHOW_ACTIONS = "show_actions";

    private IncidencesPagerAdapter.TabType tabType;
    private IncidenceAdapter.UserRole userRole;
    private boolean showActions;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressIndicator;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;

    private IncidenceAdapter adapter;
    private IncidenceApiService incidenceApiService;
    private String currentUserId;

    public static IncidenceListFragment newInstance(IncidencesPagerAdapter.TabType tabType, 
                                                   IncidenceAdapter.UserRole userRole, 
                                                   boolean showActions) {
        IncidenceListFragment fragment = new IncidenceListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TAB_TYPE, tabType);
        args.putSerializable(ARG_USER_ROLE, userRole);
        args.putBoolean(ARG_SHOW_ACTIONS, showActions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            tabType = (IncidencesPagerAdapter.TabType) getArguments().getSerializable(ARG_TAB_TYPE);
            userRole = (IncidenceAdapter.UserRole) getArguments().getSerializable(ARG_USER_ROLE);
            showActions = getArguments().getBoolean(ARG_SHOW_ACTIONS, false);
        }

        // Obtener información del usuario
        SharedPreferences sharedPrefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPrefs.getString("USER_ID", "");

        // Inicializar API Service
        ApiClient apiClient = new ApiClient(requireContext());
        incidenceApiService = new IncidenceApiService(apiClient);

        Log.d(TAG, "Fragment creado - Tipo: " + tabType + ", Rol: " + userRole + ", Acciones: " + showActions);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_incidence_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        
        loadData();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewIncidences);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
    }

    private void setupRecyclerView() {
        adapter = new IncidenceAdapter(requireContext(), userRole, showActions, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        // Deshabilitar pull-to-refresh individual 
        // La actualización se hace desde el botón principal que actualiza todas las pestañas
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadData() {
        showLoading(true);
        
        // Validar que tabType no sea null
        if (tabType == null) {
            Log.e(TAG, "tabType es null, no se puede cargar datos");
            handleErrorResponse("Error de configuración: tipo de pestaña no definido");
            return;
        }
        
        switch (tabType) {
            case ALL_INCIDENCES:
                // Solo admin y jefe de incidencias pueden ver todas
                incidenceApiService.getAllIncidences(createListCallback());
                break;
                
            case ACTIVE_INCIDENCES:
                // Solo admin y jefe de incidencias pueden ver activas
                incidenceApiService.getActiveIncidences(createListCallback());
                break;
                
            case PENDING_INCIDENCES:
                // Usar el endpoint apropiado según el rol
                if (userRole == IncidenceAdapter.UserRole.ADMIN || 
                    userRole == IncidenceAdapter.UserRole.INCIDENCE_MANAGER ||
                    userRole == IncidenceAdapter.UserRole.INCIDENCE_EMPLOYEE) {
                    // Para roles de incidencias, usar endpoint específico
                    incidenceApiService.getPendingIncidences(createListCallback());
                } else {
                    // Para otros roles, mostrar sus propias incidencias pendientes
                    loadMyFilteredIncidences(Incidence.Status.PENDIENTE);
                }
                break;
                
            case MY_ASSIGNED_INCIDENCES:
                // Solo empleados de incidencias tienen incidencias asignadas
                incidenceApiService.getAssignedIncidences(createListCallback());
                break;
                
            case MY_INCIDENCES:
                incidenceApiService.getMyIncidences(createListCallback());
                break;
                
            case DEPARTMENT_INCIDENCES:
                incidenceApiService.getDepartmentIncidences(createListCallback());
                break;
                
            // Para tipos específicos, usar endpoints o filtrado apropiado
            case IN_PROGRESS_INCIDENCES:
                if (userRole == IncidenceAdapter.UserRole.ADMIN || 
                    userRole == IncidenceAdapter.UserRole.INCIDENCE_MANAGER) {
                    // Admin y jefe de incidencias ven todas las en progreso
                    loadFilteredIncidences(Incidence.Status.EN_CURSO);
                } else {
                    // Otros roles ven solo las suyas
                    loadMyFilteredIncidences(Incidence.Status.EN_CURSO);
                }
                break;
                
            case RESOLVED_INCIDENCES:
                if (userRole == IncidenceAdapter.UserRole.ADMIN || 
                    userRole == IncidenceAdapter.UserRole.INCIDENCE_MANAGER) {
                    // Admin y jefe de incidencias ven todas las resueltas
                    loadFilteredIncidences(Incidence.Status.SOLVENTADA);
                } else {
                    // Otros roles ven solo las suyas
                    loadMyFilteredIncidences(Incidence.Status.SOLVENTADA);
                }
                break;
                
            case PENDING_DEPT:
                loadDepartmentFilteredIncidences(Incidence.Status.PENDIENTE);
                break;
                
            case RESOLVED_DEPT:
                loadDepartmentFilteredIncidences(Incidence.Status.SOLVENTADA);
                break;
                
            case MY_PENDING:
                loadMyFilteredIncidences(Incidence.Status.PENDIENTE);
                break;
                
            case MY_IN_PROGRESS:
                loadMyFilteredIncidences(Incidence.Status.EN_CURSO);
                break;
                
            case MY_RESOLVED:
                loadMyFilteredIncidences(Incidence.Status.SOLVENTADA);
                break;
                
            default:
                Log.w(TAG, "Tipo de pestaña no reconocido: " + tabType);
                showLoading(false);
                showEmptyState("Tipo de vista no implementado");
                break;
        }
    }

    private void loadFilteredIncidences(Incidence.Status statusFilter) {
        // Este método solo debe ser usado por admin y jefe de incidencias
        // que pueden acceder al endpoint /all
        incidenceApiService.getAllIncidences(new IncidenceApiService.IncidenceListCallback() {
            @Override
            public void onSuccess(List<Incidence> incidences) {
                List<Incidence> filtered = new ArrayList<>();
                for (Incidence incidence : incidences) {
                    if (incidence.getStatus() == statusFilter) {
                        filtered.add(incidence);
                    }
                }
                handleSuccessResponse(filtered);
            }

            @Override
            public void onError(String error) {
                handleErrorResponse(error);
            }
        });
    }

    private void loadDepartmentFilteredIncidences(Incidence.Status statusFilter) {
        incidenceApiService.getDepartmentIncidences(new IncidenceApiService.IncidenceListCallback() {
            @Override
            public void onSuccess(List<Incidence> incidences) {
                List<Incidence> filtered = new ArrayList<>();
                for (Incidence incidence : incidences) {
                    if (incidence.getStatus() == statusFilter) {
                        filtered.add(incidence);
                    }
                }
                handleSuccessResponse(filtered);
            }

            @Override
            public void onError(String error) {
                handleErrorResponse(error);
            }
        });
    }

    private void loadMyFilteredIncidences(Incidence.Status statusFilter) {
        incidenceApiService.getMyIncidences(new IncidenceApiService.IncidenceListCallback() {
            @Override
            public void onSuccess(List<Incidence> incidences) {
                List<Incidence> filtered = new ArrayList<>();
                for (Incidence incidence : incidences) {
                    if (incidence.getStatus() == statusFilter) {
                        filtered.add(incidence);
                    }
                }
                handleSuccessResponse(filtered);
            }

            @Override
            public void onError(String error) {
                handleErrorResponse(error);
            }
        });
    }

    private IncidenceApiService.IncidenceListCallback createListCallback() {
        return new IncidenceApiService.IncidenceListCallback() {
            @Override
            public void onSuccess(List<Incidence> incidences) {
                handleSuccessResponse(incidences);
            }

            @Override
            public void onError(String error) {
                handleErrorResponse(error);
            }
        };
    }

    private void handleSuccessResponse(List<Incidence> incidences) {
        showLoading(false);
        
        if (incidences != null && !incidences.isEmpty()) {
            adapter.updateIncidences(incidences);
            showContent();
            Log.d(TAG, "Cargadas " + incidences.size() + " incidencias para " + tabType);
        } else {
            showEmptyState(getEmptyMessage());
        }
    }

    private void handleErrorResponse(String error) {
        showLoading(false);
        Log.e(TAG, "Error cargando incidencias: " + error);
        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
        showEmptyState("Error al cargar incidencias");
    }

    private void showLoading(boolean show) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(show);
        }
        if (progressIndicator != null) {
            progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showContent() {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(String message) {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        }
        if (emptyStateText != null) {
            emptyStateText.setText(message);
        }
    }

    private String getEmptyMessage() {
        // Validar que tabType no sea null
        if (tabType == null) {
            return "No hay incidencias disponibles";
        }
        
        switch (tabType) {
            case PENDING_INCIDENCES:
                return "No hay incidencias pendientes";
            case IN_PROGRESS_INCIDENCES:
                return "No hay incidencias en curso";
            case RESOLVED_INCIDENCES:
                return "No hay incidencias resueltas";
            case MY_ASSIGNED_INCIDENCES:
                return "No tienes incidencias asignadas";
            case MY_INCIDENCES:
                return "No has creado incidencias";
            case DEPARTMENT_INCIDENCES:
                return "No hay incidencias en tu departamento";
            default:
                return "No hay incidencias disponibles";
        }
    }

    // **IMPLEMENTACIÓN DE OnIncidenceActionListener**

    @Override
    public void onIncidenceClick(Incidence incidence) {
        // TODO: Mostrar detalles de la incidencia
        Toast.makeText(requireContext(), "Detalles: " + incidence.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAcceptIncidence(Incidence incidence) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Aceptar Incidencia")
                .setMessage("¿Estás seguro de que quieres aceptar esta incidencia?\n\n" + incidence.getTitle())
                .setPositiveButton("Aceptar", (dialog, which) -> performAcceptIncidence(incidence))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onResolveIncidence(Incidence incidence) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Resolver Incidencia")
                .setMessage("¿Estás seguro de que quieres marcar esta incidencia como resuelta?\n\n" + incidence.getTitle())
                .setPositiveButton("Resolver", (dialog, which) -> performResolveIncidence(incidence))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDeleteIncidence(Incidence incidence) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar Incidencia")
                .setMessage("¿Estás seguro de que quieres eliminar esta incidencia?\n\n" + incidence.getTitle() + "\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> performDeleteIncidence(incidence))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performAcceptIncidence(Incidence incidence) {
        incidenceApiService.acceptIncidence(incidence.getId(), new IncidenceApiService.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Incidencia aceptada exitosamente", Toast.LENGTH_SHORT).show();
                // Recargar datos para actualizar la lista
                loadData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Error al aceptar: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performResolveIncidence(Incidence incidence) {
        incidenceApiService.resolveIncidence(incidence.getId(), new IncidenceApiService.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Incidencia resuelta exitosamente", Toast.LENGTH_SHORT).show();
                // Recargar datos para actualizar la lista
                loadData();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Error al resolver: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performDeleteIncidence(Incidence incidence) {
        incidenceApiService.deleteIncidence(incidence.getId(), new IncidenceApiService.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(), "Incidencia eliminada exitosamente", Toast.LENGTH_SHORT).show();
                adapter.removeIncidence(incidence);
                notifyParentOfRemoval(incidence);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Error al eliminar: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notifyParentOfUpdate(Incidence updatedIncidence) {
        if (getParentFragment() instanceof IncidencesFragment) {
            ((IncidencesFragment) getParentFragment()).updateIncidence(updatedIncidence);
        }
    }

    private void notifyParentOfRemoval(Incidence incidence) {
        if (getParentFragment() instanceof IncidencesFragment) {
            ((IncidencesFragment) getParentFragment()).removeIncidence(incidence);
        }
    }



    // **MÉTODOS PÚBLICOS PARA NOTIFICACIONES EXTERNAS**

    public void onIncidenceCreated(Incidence newIncidence) {
        // Verificar que el fragmento esté completamente inicializado
        if (adapter == null || tabType == null) {
            Log.w(TAG, "Fragmento no inicializado completamente, ignorando notificación de incidencia creada");
            return;
        }
        
        if (shouldShowIncidence(newIncidence)) {
            adapter.addIncidence(newIncidence);
            showContent();
        }
    }

    public void onIncidenceUpdated(Incidence updatedIncidence) {
        // Verificar que el fragmento esté completamente inicializado
        if (adapter == null || tabType == null) {
            Log.w(TAG, "Fragmento no inicializado completamente, ignorando notificación de incidencia actualizada");
            return;
        }
        
        if (shouldShowIncidence(updatedIncidence)) {
            adapter.updateIncidence(updatedIncidence);
        } else {
            adapter.removeIncidence(updatedIncidence);
        }
    }

    public void onIncidenceRemoved(Incidence incidence) {
        // Verificar que el fragmento esté completamente inicializado
        if (adapter == null) {
            Log.w(TAG, "Fragmento no inicializado completamente, ignorando notificación de incidencia eliminada");
            return;
        }
        
        adapter.removeIncidence(incidence);
    }

    public void refreshData() {
        loadData();
    }

    private boolean shouldShowIncidence(Incidence incidence) {
        // Validar que tabType no sea null para evitar NullPointerException
        if (tabType == null) {
            Log.w(TAG, "tabType es null, mostrando incidencia por defecto");
            return true;
        }
        
        switch (tabType) {
            case PENDING_INCIDENCES:
                return incidence.getStatus() == Incidence.Status.PENDIENTE;
            case IN_PROGRESS_INCIDENCES:
                return incidence.getStatus() == Incidence.Status.EN_CURSO;
            case RESOLVED_INCIDENCES:
                return incidence.getStatus() == Incidence.Status.SOLVENTADA;
            case ACTIVE_INCIDENCES:
                return incidence.isActive();
            case MY_ASSIGNED_INCIDENCES:
                return currentUserId.equals(incidence.getAssignedTo());
            case MY_INCIDENCES:
                return currentUserId.equals(incidence.getCreatedBy());
            // Agregar más casos según sea necesario
            default:
                return true;
        }
    }
} 