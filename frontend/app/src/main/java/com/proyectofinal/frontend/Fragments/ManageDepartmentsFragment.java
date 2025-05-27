package com.proyectofinal.frontend.Fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.proyectofinal.frontend.Adapters.DepartmentAdapter;
import com.proyectofinal.frontend.Adapters.EmployeeSearchAdapter;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.DepartmentApiService;
import com.proyectofinal.frontend.Models.Department;
import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageDepartmentsFragment extends Fragment implements DepartmentAdapter.OnDepartmentClickListener, EmployeeSearchAdapter.OnEmployeeSelectedListener {

    // Variables para las vistas
    private View rootView;
    private ConstraintLayout listContainer;
    private ConstraintLayout formContainer;
    private RecyclerView departmentsRecyclerView;
    private FloatingActionButton addDepartmentButton;
    private TextView formTitleTextView;
    private TextView emptyStateTextView;
    private EditText departmentNameEditText, departmentDescriptionEditText, employeeSearchEditText;
    private Button saveDepartmentButton, cancelDepartmentButton;
    private RecyclerView employeeSearchRecyclerView;
    private TextView selectedManagerLabelTextView, selectedManagerTextView;
    private ProgressBar loadingProgressBar;

    // Variables para manejar los datos
    private DepartmentAdapter departmentAdapter;
    private EmployeeSearchAdapter employeeSearchAdapter;
    private DepartmentApiService apiService;
    private boolean isEditMode = false;
    private String departmentIdToEdit;
    private int currentEditPosition = -1;
    
    // Variables para el jefe de departamento
    private Employee selectedManager;
    
    // Handler para controlar la búsqueda con delay
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY_MS = 500; // Tiempo de espera entre escritura y búsqueda
    
    // Clave para SharedPreferences para saber si ya se mostró la guía
    private static final String PREF_DEPARTMENT_GUIDE_SHOWN = "department_guide_shown";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_departments, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar las referencias a las vistas
        initViews(view);

        // Configurar RecyclerView
        setupRecyclerViews();

        // Inicializar el servicio Retrofit
        initRetrofit();

        // Configurar listeners
        setupListeners();

        // Mostrar la lista de departamentos directamente
        fetchDepartments();
    }

    private void initViews(View view) {
        listContainer = view.findViewById(R.id.listContainer);
        formContainer = view.findViewById(R.id.formContainer);
        departmentsRecyclerView = view.findViewById(R.id.departmentsRecyclerView);
        addDepartmentButton = view.findViewById(R.id.addDepartmentButton);
        formTitleTextView = view.findViewById(R.id.formTitleTextView);
        departmentNameEditText = view.findViewById(R.id.departmentNameEditText);
        departmentDescriptionEditText = view.findViewById(R.id.departmentDescriptionEditText);
        saveDepartmentButton = view.findViewById(R.id.saveDepartmentButton);
        cancelDepartmentButton = view.findViewById(R.id.cancelDepartmentButton);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
        
        // Vistas para búsqueda de empleados
        employeeSearchEditText = view.findViewById(R.id.employeeSearchEditText);
        employeeSearchRecyclerView = view.findViewById(R.id.employeeSearchRecyclerView);
        selectedManagerLabelTextView = view.findViewById(R.id.selectedManagerLabelTextView);
        selectedManagerTextView = view.findViewById(R.id.selectedManagerTextView);
    }

    private void setupRecyclerViews() {
        // RecyclerView de departamentos
        departmentAdapter = new DepartmentAdapter(this);
        departmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        departmentsRecyclerView.setAdapter(departmentAdapter);
        
        // RecyclerView de búsqueda de empleados
        employeeSearchAdapter = new EmployeeSearchAdapter(this);
        employeeSearchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        employeeSearchRecyclerView.setAdapter(employeeSearchAdapter);
    }

    private void initRetrofit() {
        // Usar ApiClient para obtener un cliente ya configurado con token JWT
        if (getContext() != null) {
            ApiClient apiClient = ApiClient.getInstance(getContext());
            
            // Crear el servicio para departamentos usando la URL base y cliente HTTP ya configurados
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(apiClient.getCurrentBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(apiClient.getOkHttpClient()) // Usar el cliente HTTP configurado con token
                    .build();
            
            apiService = retrofit.create(DepartmentApiService.class);
        } else {
            // Si el contexto es nulo, registrar error pero no mostrar Toast
            System.out.println("Error: Contexto nulo al inicializar API");
        }
    }

    private void setupListeners() {
        // Botón flotante para crear un departamento
        addDepartmentButton.setOnClickListener(v -> {
            showFormForCreate();
        });

        // Listener para el botón Guardar
        saveDepartmentButton.setOnClickListener(v -> {
            if (validateForm()) {
                if (isEditMode) {
                    updateDepartment();
                } else {
                    createDepartment();
                }
            }
        });

        // Listener para el botón Cancelar
        cancelDepartmentButton.setOnClickListener(v -> hideForm());
        
        // Configurar búsqueda dinámica con TextWatcher
        employeeSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No es necesario implementar
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancelar cualquier búsqueda pendiente
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String query = s.toString().trim();
                
                // Si el campo está vacío, ocultar los resultados
                if (query.isEmpty()) {
                    employeeSearchRecyclerView.setVisibility(View.GONE);
                    return;
                }
                
                // Crear una nueva búsqueda con delay para evitar muchas peticiones
                searchRunnable = () -> {
                    if (query.length() >= 2) { // Buscar solo si hay al menos 2 caracteres
                        searchEmployees(query);
                    }
                };
                
                // Ejecutar la búsqueda después de un delay
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
        });
    }
    
    private void showListContainer() {
        listContainer.setVisibility(View.VISIBLE);
        formContainer.setVisibility(View.GONE);
    }
    
    private void showFormContainer() {
        listContainer.setVisibility(View.GONE);
        formContainer.setVisibility(View.VISIBLE);
    }

    private boolean validateForm() {
        String name = departmentNameEditText.getText().toString().trim();
        String description = departmentDescriptionEditText.getText().toString().trim();

        if (name.isEmpty()) {
            departmentNameEditText.setError("El nombre del departamento es obligatorio");
            return false;
        }

        return true;
    }

    private void showFormForCreate() {
        isEditMode = false;
        departmentIdToEdit = null;
        currentEditPosition = -1;
        selectedManager = null;

        // Limpiar el formulario
        departmentNameEditText.setText("");
        departmentDescriptionEditText.setText("");
        employeeSearchEditText.setText("");
        employeeSearchRecyclerView.setVisibility(View.GONE);
        selectedManagerLabelTextView.setVisibility(View.GONE);
        selectedManagerTextView.setVisibility(View.GONE);

        // Configurar título y visibilidad
        formTitleTextView.setText("Crear Departamento");
        saveDepartmentButton.setText("Guardar");

        // Mostrar el formulario
        showFormContainer();
    }

    private void showFormForEdit(Department department, int position) {
        isEditMode = true;
        departmentIdToEdit = department.getId();
        currentEditPosition = position;
        selectedManager = null;

        // Rellenar el formulario con los datos existentes
        departmentNameEditText.setText(department.getName());
        departmentDescriptionEditText.setText(department.getDescription());
        employeeSearchEditText.setText("");
        employeeSearchRecyclerView.setVisibility(View.GONE);
        
        // Configurar título y visibilidad
        formTitleTextView.setText("Editar Departamento");
        saveDepartmentButton.setText("Actualizar");
        
        // Comprobar si hay un jefe de departamento
        if (department.getManagerId() != null && !department.getManagerId().isEmpty()) {
            // Cargar la información del jefe actual desde la API
            fetchEmployeeInfo(department.getManagerId());
        } else {
            selectedManagerLabelTextView.setVisibility(View.GONE);
            selectedManagerTextView.setVisibility(View.GONE);
        }

        // Mostrar el formulario
        showFormContainer();
    }
    
    private void fetchEmployeeInfo(String employeeId) {
        showLoading(true);
        // Esta función debería implementarse para obtener la información del empleado por su ID
        // Como no tenemos ese endpoint, mostramos un mensaje alternativo
        selectedManagerLabelTextView.setVisibility(View.VISIBLE);
        selectedManagerTextView.setVisibility(View.VISIBLE);
        selectedManagerTextView.setText("ID del jefe actual: " + employeeId);
        showLoading(false);
    }

    private void hideForm() {
        // Volver a la lista de departamentos
        showListContainer();
        
        // Limpiar errores si los hay
        departmentNameEditText.setError(null);
        
        // Limpiar búsqueda
        employeeSearchEditText.setText("");
        employeeSearchRecyclerView.setVisibility(View.GONE);
    }

    // Mostrar estado vacío (cuando no hay departamentos)
    private void showEmptyState(boolean show) {
        if (emptyStateTextView != null) {
            emptyStateTextView.setVisibility(show ? View.VISIBLE : View.GONE);
            departmentsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
        
        // Si mostramos estado vacío (no hay departamentos) mostramos la guía
        if (show) {
            checkAndShowCreateDepartmentGuide();
        }
    }
    
    // Verificar si la guía ya se mostró previamente
    private boolean isGuidePreviouslyShown() {
        if (getContext() == null) return false;
        return getContext().getSharedPreferences("app_prefs", 0)
                .getBoolean(PREF_DEPARTMENT_GUIDE_SHOWN, false);
    }
    
    // Guardar que la guía ya se mostró
    private void saveGuideShown() {
        if (getContext() == null) return;
        getContext().getSharedPreferences("app_prefs", 0)
                .edit()
                .putBoolean(PREF_DEPARTMENT_GUIDE_SHOWN, true)
                .apply();
    }
    
    // Mostrar guía para el botón de crear departamento cuando no hay departamentos
    private void checkAndShowCreateDepartmentGuide() {
        // Verificar si ya se mostró la guía anteriormente
        if (getContext() == null || isGuidePreviouslyShown() || !isAdded()) {
            return;
        }
        
        // Esperar a que las vistas estén listas
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getContext() == null || !isAdded()) return;
            
            // Crear la secuencia de guía
            new TapTargetSequence(requireActivity())
                .targets(
                    TapTarget.forView(addDepartmentButton, "Crear departamento", "Pulsa aquí para crear un nuevo departamento")
                        .outerCircleColor(R.color.colorPrimary)
                        .targetCircleColor(R.color.white)
                        .titleTextSize(20)
                        .titleTextColor(R.color.white)
                        .descriptionTextSize(14)
                        .descriptionTextColor(R.color.white)
                        .textTypeface(Typeface.SANS_SERIF)
                        .dimColor(R.color.black)
                        .drawShadow(true)
                        .cancelable(true)
                        .tintTarget(true)
                        .transparentTarget(true)
                        .targetRadius(60)
                )
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        // Guardar que ya se mostró la guía
                        saveGuideShown();
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        // No se necesita hacer nada aquí
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        // Guardar que ya se mostró la guía incluso si se cancela
                        saveGuideShown();
                    }
                })
                .start();
        }, 1000);
    }
    
    // Buscar empleados por nombre - ahora es dinámica
    private void searchEmployees(String query) {
        // Mostrar un indicador de carga discreto durante la búsqueda
        employeeSearchRecyclerView.setVisibility(View.VISIBLE);
        
        if (employeeSearchAdapter.getItemCount() == 0) {
            // Si no hay resultados previos, mostrar un indicador de carga
            showLoading(true);
        }
        
        apiService.searchEmployees(query).enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Employee> employees = response.body();
                    if (employees.isEmpty()) {
                        // Si no hay resultados, mostrar un mensaje
                        Toast.makeText(getContext(), "No se encontraron empleados con ese nombre", Toast.LENGTH_SHORT).show();
                        employeeSearchRecyclerView.setVisibility(View.GONE);
                    } else {
                        // Actualizar la lista de resultados
                        employeeSearchAdapter.setEmployees(employees);
                        employeeSearchRecyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    showErrorMessage("Error al buscar empleados: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"));
                    employeeSearchRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
                employeeSearchRecyclerView.setVisibility(View.GONE);
            }
        });
    }
    
    // Método llamado cuando se selecciona un empleado
    @Override
    public void onEmployeeSelected(Employee employee) {
        selectedManager = employee;
        selectedManagerLabelTextView.setVisibility(View.VISIBLE);
        selectedManagerTextView.setVisibility(View.VISIBLE);
        selectedManagerTextView.setText(employee.getFullName() + " (" + employee.getEmail() + ")");
        employeeSearchRecyclerView.setVisibility(View.GONE);
        employeeSearchEditText.setText(""); // Limpiar el campo de búsqueda
    }
    
    private void fetchDepartments() {
        showLoading(true);
        apiService.getAllDepartments().enqueue(new Callback<List<Department>>() {
            @Override
            public void onResponse(Call<List<Department>> call, Response<List<Department>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Department> departments = response.body();
                    
                    // Mostrar los departamentos en el RecyclerView
                    departmentAdapter.setDepartments(departments);
                    
                    // Mostrar estado vacío si no hay departamentos
                    showEmptyState(departments.isEmpty());
                } else {
                    showErrorMessage("Error al cargar departamentos: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"));
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<List<Department>> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
                showEmptyState(true);
            }
        });
    }

    private void createDepartment() {
        String name = departmentNameEditText.getText().toString().trim();
        String description = departmentDescriptionEditText.getText().toString().trim();
        
        Department newDepartment = new Department();
        newDepartment.setName(name);
        newDepartment.setDescription(description);
        
        // Asignar jefe si se ha seleccionado uno
        if (selectedManager != null) {
            newDepartment.setManagerId(selectedManager.getId());
        }
        
        showLoading(true);
        apiService.createDepartment(newDepartment).enqueue(new Callback<Department>() {
            @Override
            public void onResponse(Call<Department> call, Response<Department> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Department createdDepartment = response.body();
                    
                    // Si seleccionamos un jefe, hacemos una segunda llamada para asignar el jefe
                    if (selectedManager != null) {
                        assignManager(createdDepartment.getId(), selectedManager.getId());
                    } else {
                        // Mostrar mensaje de éxito
                        showSuccessMessage("Departamento creado correctamente");
                        
                        // Cargar de nuevo la lista completa
                        fetchDepartments();
                        
                        // Esconder el formulario
                        hideForm();
                    }
                } else {
                    showErrorMessage("Error al crear departamento: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"));
                }
            }

            @Override
            public void onFailure(Call<Department> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void updateDepartment() {
        String name = departmentNameEditText.getText().toString().trim();
        String description = departmentDescriptionEditText.getText().toString().trim();
        
        Department updatedDepartment = new Department();
        updatedDepartment.setName(name);
        updatedDepartment.setDescription(description);
        
        // Asignar jefe si se ha seleccionado uno
        if (selectedManager != null) {
            updatedDepartment.setManagerId(selectedManager.getId());
        }
        
        showLoading(true);
        apiService.updateDepartment(departmentIdToEdit, updatedDepartment).enqueue(new Callback<Department>() {
            @Override
            public void onResponse(Call<Department> call, Response<Department> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Department updatedDept = response.body();
                    
                    // Si seleccionamos un jefe, hacemos una segunda llamada para asignar el jefe
                    if (selectedManager != null) {
                        assignManager(departmentIdToEdit, selectedManager.getId());
                    } else {
                        // Mostrar mensaje de éxito
                        showSuccessMessage("Departamento actualizado correctamente");
                        
                        // Actualizar el departamento en el RecyclerView
                        departmentAdapter.updateDepartment(updatedDept, currentEditPosition);
                        
                        // Esconder el formulario
                        hideForm();
                    }
                } else {
                    showErrorMessage("Error al actualizar departamento: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"));
                }
            }

            @Override
            public void onFailure(Call<Department> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    private void assignManager(String departmentId, String employeeId) {
        showLoading(true);
        
        Map<String, String> request = new HashMap<>();
        request.put("employeeId", employeeId);
        
        apiService.assignManager(departmentId, request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    // Mostrar mensaje de éxito
                    showSuccessMessage("Jefe de departamento asignado correctamente");
                    
                    // Cargar de nuevo la lista completa
                    fetchDepartments();
                    
                    // Esconder el formulario
                    hideForm();
                } else {
                    showErrorMessage("Error al asignar jefe de departamento: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"));
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión al asignar jefe: " + t.getMessage());
            }
        });
    }

    private void deleteDepartment(Department department, int position) {
        showLoading(true);
        apiService.deleteDepartment(department.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    // Eliminar del adapter
                    departmentAdapter.removeDepartment(position);
                    
                    // Mostrar mensaje
                    showSuccessMessage("Departamento eliminado correctamente");
                    
                    // Mostrar estado vacío si ya no hay departamentos
                    if (departmentAdapter.getItemCount() == 0) {
                        showEmptyState(true);
                    }
                } else {
                    showErrorMessage("Error al eliminar departamento: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showConfirmDeleteDialog(Department department, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar departamento")
                .setMessage("¿Estás seguro que deseas eliminar este departamento?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteDepartment(department, position))
                .setNegativeButton("Cancelar", null)
                .show();
    }
    
    private void showLoading(boolean show) {
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showErrorMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSuccessMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    
    // Implementación de los métodos de OnDepartmentClickListener
    @Override
    public void onEditDepartment(Department department, int position) {
        showFormForEdit(department, position);
    }

    @Override
    public void onDeleteDepartment(Department department, int position) {
        showConfirmDeleteDialog(department, position);
    }
} 