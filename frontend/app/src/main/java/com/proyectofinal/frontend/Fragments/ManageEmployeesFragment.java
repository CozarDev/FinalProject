package com.proyectofinal.frontend.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.proyectofinal.frontend.Adapters.EmployeeAdapter;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.DepartmentApiService;
import com.proyectofinal.frontend.Api.EmployeeApiService;
import com.proyectofinal.frontend.Models.Department;
import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ManageEmployeesFragment extends Fragment implements EmployeeAdapter.OnEmployeeClickListener {

    // Variables para las vistas
    private View rootView;
    private ConstraintLayout listContainer;
    private ConstraintLayout formContainer;
    private RecyclerView employeesRecyclerView;
    private FloatingActionButton addEmployeeButton;
    private TextView formTitleTextView;
    private TextView emptyStateTextView;
    private EditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText, usernameEditText, passwordEditText;
    private AutoCompleteTextView departmentAutoCompleteTextView;
    private Button saveEmployeeButton, cancelEmployeeButton;
    private ProgressBar loadingProgressBar;

    // Variables para manejar los datos
    private EmployeeAdapter adapter;
    private EmployeeApiService employeeApiService;
    private DepartmentApiService departmentApiService;
    private boolean isEditMode = false;
    private String employeeIdToEdit;
    private int currentEditPosition = -1;
    
    // Lista de departamentos
    private List<Department> departments = new ArrayList<>();
    private Map<String, String> departmentMap = new HashMap<>(); // id -> name
    private Map<String, String> departmentNameToIdMap = new HashMap<>(); // name -> id

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_manage_employees, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar las referencias a las vistas
        initViews(view);

        // Configurar RecyclerView
        setupRecyclerView();

        // Inicializar el servicio Retrofit
        initRetrofit();

        // Configurar listeners
        setupListeners();

        // Cargar departamentos primero
        fetchDepartments();
    }

    private void initViews(View view) {
        listContainer = view.findViewById(R.id.listContainer);
        formContainer = view.findViewById(R.id.formContainer);
        employeesRecyclerView = view.findViewById(R.id.employeesRecyclerView);
        addEmployeeButton = view.findViewById(R.id.addEmployeeButton);
        formTitleTextView = view.findViewById(R.id.formTitleTextView);
        
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        departmentAutoCompleteTextView = view.findViewById(R.id.departmentAutoCompleteTextView);
        
        saveEmployeeButton = view.findViewById(R.id.saveEmployeeButton);
        cancelEmployeeButton = view.findViewById(R.id.cancelEmployeeButton);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
    }

    private void setupRecyclerView() {
        adapter = new EmployeeAdapter(this);
        employeesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        employeesRecyclerView.setAdapter(adapter);
    }

    private void initRetrofit() {
        // Usar ApiClient para obtener un cliente ya configurado con token JWT
        if (getContext() != null) {
            ApiClient apiClient = ApiClient.getInstance(getContext());
            
            // Crear el servicio para empleados usando la URL base y cliente HTTP ya configurados
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ApiClient.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(apiClient.getOkHttpClient()) // Usar el cliente HTTP configurado con token
                    .build();
            
            employeeApiService = retrofit.create(EmployeeApiService.class);
            departmentApiService = retrofit.create(DepartmentApiService.class);
        } else {
            // Si el contexto es nulo, registrar error pero no mostrar Toast
            System.out.println("Error: Contexto nulo al inicializar API");
        }
    }

    private void setupListeners() {
        // Botón flotante para crear un empleado
        addEmployeeButton.setOnClickListener(v -> {
            showFormForCreate();
        });

        // Listener para el botón Guardar
        saveEmployeeButton.setOnClickListener(v -> {
            if (validateForm()) {
                if (isEditMode) {
                    updateEmployee();
                } else {
                    createEmployee();
                }
            }
        });

        // Listener para el botón Cancelar
        cancelEmployeeButton.setOnClickListener(v -> hideForm());
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
        boolean isValid = true;
        
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String department = departmentAutoCompleteTextView.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        if (firstName.isEmpty()) {
            firstNameEditText.setError("El nombre es obligatorio");
            isValid = false;
        }
        
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Los apellidos son obligatorios");
            isValid = false;
        }
        
        if (email.isEmpty()) {
            emailEditText.setError("El email es obligatorio");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Introduce un email válido");
            isValid = false;
        }
        
        if (department.isEmpty() || !departmentNameToIdMap.containsKey(department)) {
            departmentAutoCompleteTextView.setError("Selecciona un departamento válido");
            isValid = false;
        }
        
        if (username.isEmpty()) {
            usernameEditText.setError("El nombre de usuario es obligatorio");
            isValid = false;
        }
        
        // La contraseña es obligatoria solo al crear, no al editar
        if (!isEditMode && password.isEmpty()) {
            passwordEditText.setError("La contraseña es obligatoria");
            isValid = false;
        }
        
        return isValid;
    }

    private void showFormForCreate() {
        isEditMode = false;
        employeeIdToEdit = null;
        currentEditPosition = -1;

        // Limpiar el formulario
        firstNameEditText.setText("");
        lastNameEditText.setText("");
        emailEditText.setText("");
        phoneEditText.setText("");
        usernameEditText.setText("");
        passwordEditText.setText("");
        departmentAutoCompleteTextView.setText("");

        // Configurar título y visibilidad
        formTitleTextView.setText("Crear Empleado");
        saveEmployeeButton.setText("Guardar");

        // Mostrar el formulario
        showFormContainer();
    }

    private void showFormForEdit(Employee employee, int position) {
        isEditMode = true;
        employeeIdToEdit = employee.getId();
        currentEditPosition = position;
        
        formTitleTextView.setText("Editar Empleado");
        firstNameEditText.setText(employee.getFirstName());
        lastNameEditText.setText(employee.getLastName());
        emailEditText.setText(employee.getEmail());
        phoneEditText.setText(employee.getPhone());
        
        // Cargar información del usuario asociado
        loadEmployeeUserInfo(employee.getId());
        
        // Seleccionar el departamento
        if (employee.getDepartmentName() != null && !employee.getDepartmentName().isEmpty()) {
            departmentAutoCompleteTextView.setText(employee.getDepartmentName());
        } else if (employee.getDepartmentId() != null && departmentMap.containsKey(employee.getDepartmentId())) {
            departmentAutoCompleteTextView.setText(departmentMap.get(employee.getDepartmentId()));
        }
        
        showFormContainer();
    }
    
    private void loadEmployeeUserInfo(String employeeId) {
        employeeApiService.getEmployeeUserInfo(employeeId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> userInfo = response.body();
                    String username = userInfo.get("username");
                    String password = userInfo.get("password");
                    
                    if (username != null) {
                        usernameEditText.setText(username);
                    }
                    if (password != null) {
                        passwordEditText.setText(password);
                    }
                } else {
                    // Error al cargar la información del usuario
                    Toast.makeText(getContext(), "No se pudo cargar la información del usuario", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideForm() {
        // Volver a la lista de empleados
        showListContainer();
        
        // Limpiar errores si los hay
        firstNameEditText.setError(null);
        lastNameEditText.setError(null);
        emailEditText.setError(null);
        ((TextInputLayout) departmentAutoCompleteTextView.getParent().getParent()).setError(null);
    }

    // Mostrar estado vacío (cuando no hay empleados)
    private void showEmptyState(boolean show) {
        if (emptyStateTextView != null) {
            emptyStateTextView.setVisibility(show ? View.VISIBLE : View.GONE);
            employeesRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    // Cargar departamentos para el desplegable
    private void setupDepartmentsDropdown() {
        if (getContext() == null || departments == null) return;
        
        // Crear mapa de departamentos (id -> nombre y nombre -> id)
        departmentMap.clear();
        departmentNameToIdMap.clear();
        
        List<String> departmentNames = new ArrayList<>();
        for (Department department : departments) {
            String id = department.getId();
            String name = department.getName();
            
            departmentMap.put(id, name);
            departmentNameToIdMap.put(name, id);
            departmentNames.add(name);
        }
        
        // Crear y configurar adaptador para el AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                departmentNames);
        
        departmentAutoCompleteTextView.setAdapter(adapter);
    }
    
    // Fetch departments from API
    private void fetchDepartments() {
        showLoading(true);
        
        departmentApiService.getAllDepartments().enqueue(new Callback<List<Department>>() {
            @Override
            public void onResponse(Call<List<Department>> call, Response<List<Department>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departments = response.body();
                    setupDepartmentsDropdown();
                    
                    // Una vez cargados los departamentos, cargar los empleados
                    fetchEmployees();
                } else {
                    showLoading(false);
                    showErrorMessage("Error al cargar los departamentos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Department>> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    // Fetch all employees from API
    private void fetchEmployees() {
        employeeApiService.getAllEmployees().enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    List<Employee> employees = response.body();
                    if (employees != null && !employees.isEmpty()) {
                        adapter.setEmployees(employees, departmentMap);
                        showEmptyState(false);
                    } else {
                        // No hay empleados, mostrar estado vacío
                        showEmptyState(true);
                    }
                } else {
                    // Error en la respuesta, asumimos que no hay empleados aún
                    showEmptyState(true);
                    
                    // Solo mostrar mensaje de error si no es un 404 (not found)
                    if (response.code() != 404) {
                        showErrorMessage("Error al cargar los empleados: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                showLoading(false);
                showEmptyState(true);
            }
        });
    }

    // Create a new employee
    private void createEmployee() {
        if (!validateForm()) {
            return;
        }
        
        showLoading(true);
        
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String departmentId = departmentNameToIdMap.get(departmentAutoCompleteTextView.getText().toString().trim());
        
        // Crear nuevo empleado
        Employee employee = new Employee(
                firstName,
                lastName,
                email,
                phone,
                username,
                password,
                departmentId
        );
        
        employeeApiService.createEmployee(employee).enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    // Asignar el departmentName para mostrarlo correctamente en la lista
                    Employee createdEmployee = response.body();
                    createdEmployee.setDepartmentName(departmentMap.get(createdEmployee.getDepartmentId()));
                    createdEmployee.setUsername(username);
                    createdEmployee.setPassword(password);
                    
                    // Agregar a la lista
                    adapter.addEmployee(createdEmployee);
                    
                    // Mostrar mensaje de éxito
                    showSuccessMessage("Empleado creado correctamente");
                    
                    // Ocultar el formulario
                    hideForm();
                } else {
                    showErrorMessage("Error al crear el empleado: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
            }
        });
    }

    // Update an existing employee
    private void updateEmployee() {
        if (!validateForm()) {
            return;
        }
        
        showLoading(true);
        
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String departmentId = departmentNameToIdMap.get(departmentAutoCompleteTextView.getText().toString().trim());
        
        // Crear objeto con datos actualizados
        Employee employee = new Employee(
                firstName,
                lastName,
                email,
                phone,
                username,
                password,
                departmentId
        );
        employee.setId(employeeIdToEdit);
        
        employeeApiService.updateEmployee(employeeIdToEdit, employee).enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    // Actualizar la lista
                    Employee updatedEmployee = response.body();
                    updatedEmployee.setDepartmentName(departmentMap.get(updatedEmployee.getDepartmentId()));
                    updatedEmployee.setUsername(username);
                    updatedEmployee.setPassword(password);
                    
                    adapter.updateEmployee(currentEditPosition, updatedEmployee);
                    
                    // Mostrar mensaje de éxito
                    showSuccessMessage("Empleado actualizado correctamente");
                    
                    // Ocultar formulario
                    hideForm();
                } else {
                    showErrorMessage("Error al actualizar el empleado: " + response.message());
                }
            }
            
            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
            }
        });
    }

    // Delete an employee
    private void deleteEmployee(Employee employee, int position) {
        showLoading(true);

        employeeApiService.deleteEmployee(employee.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                showLoading(false);
                
                if (response.isSuccessful()) {
                    adapter.removeEmployee(position);
                    showSuccessMessage("Empleado eliminado correctamente");
                    
                    // Verificar si ahora la lista está vacía
                    if (adapter.getItemCount() == 0) {
                        showEmptyState(true);
                    }
                } else {
                    showErrorMessage("Error al eliminar el empleado");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showLoading(false);
                showErrorMessage("Error de conexión: " + t.getMessage());
            }
        });
    }

    private void showConfirmDeleteDialog(Employee employee, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Empleado")
                .setMessage("¿Estás seguro de que quieres eliminar a " + employee.getFullName() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteEmployee(employee, position))
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
            // Mostrar un Toast simple
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccessMessage(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Implementación de la interfaz OnEmployeeClickListener del adaptador
    @Override
    public void onEditEmployee(Employee employee, int position) {
        showFormForEdit(employee, position);
    }

    @Override
    public void onDeleteEmployee(Employee employee, int position) {
        showConfirmDeleteDialog(employee, position);
    }
} 