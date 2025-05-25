package com.proyectofinal.frontend.Fragments;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.proyectofinal.frontend.Adapters.ShiftAssignmentAdapter;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Models.Department;
import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.Models.ShiftAssignment;
import com.proyectofinal.frontend.Models.ShiftType;
import com.proyectofinal.frontend.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.text.ParseException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ManageShiftAssignmentsFragment extends Fragment {
    
    private static final String TAG = "ShiftAssignmentsFrag";
    private RecyclerView recyclerView;
    private FloatingActionButton fabAddShiftAssignment;
    private ProgressBar progressBar;
    private ApiClient apiClient;
    private boolean isAdmin = false;
    private boolean isDepartmentHead = false;
    private String userId = "";
    private String departmentId = "";
    private List<Employee> departmentEmployees = new ArrayList<>();
    private List<ShiftType> availableShiftTypes = new ArrayList<>();
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_shift_assignments, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar vistas
        recyclerView = view.findViewById(R.id.rvShiftAssignments);
        fabAddShiftAssignment = view.findViewById(R.id.fabAddShiftAssignment);
        progressBar = view.findViewById(R.id.progressBar);
        
        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Inicializar API client
        apiClient = ApiClient.getInstance(getContext());
        
        // Comprobar el rol del usuario
        checkUserRole();
        
        // Configurar botón para añadir asignación de turno
        fabAddShiftAssignment.setOnClickListener(v -> showAddShiftAssignmentDialog());
        
        // Cargar datos según el rol
        loadData();
    }
    
    private void checkUserRole() {
        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "EMPLOYEE");
        userId = prefs.getString("USER_ID", "");
        
        isAdmin = "ADMIN".equals(role);
        isDepartmentHead = "DEPARTMENT_HEAD".equals(role);
        
        Log.d(TAG, "Rol del usuario: " + role);
        Log.d(TAG, "isAdmin: " + isAdmin + ", isDepartmentHead: " + isDepartmentHead);
        
        if (isDepartmentHead) {
            // Si es jefe de departamento, necesitamos obtener su departamento
            getDepartmentForManager();
        }
    }
    
    private void getDepartmentForManager() {
        showLoading(true);
        
        // Añadir log para depurar
        Log.d(TAG, "Solicitando departamento para jefe con ID: " + userId);
        
        // Buscar de qué departamento es jefe
        apiClient.getDepartmentApiService().getDepartmentByManagerId(userId).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                showLoading(false);
                
                // Añadir logs para depurar la respuesta
                Log.d(TAG, "Respuesta recibida - código: " + response.code());
                if (!response.isSuccessful()) {
                    try {
                        Log.e(TAG, "Error en la respuesta: " + response.errorBody().string());
                    } catch (Exception e) {
                        Log.e(TAG, "No se pudo leer el error: " + e.getMessage());
                    }
                } else {
                    Log.d(TAG, "Respuesta exitosa, cuerpo vacío? " + (response.body() == null || response.body().isEmpty()));
                }
                
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    departmentId = response.body().get(0);
                    Log.d(TAG, "Departamento obtenido: " + departmentId);
                    
                    // Una vez tenemos el departamento, cargamos los empleados de ese departamento
                    loadEmployeesForDepartment();
                } else {
                    // Si falla, intenta obtener el departamento por otra vía
                    Log.d(TAG, "Intentando obtener departamento del empleado directamente");
                    getEmployeeInformation();
                }
            }
            
            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error al obtener departamento: ", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                
                // Si falla, intenta obtener el departamento por otra vía
                Log.d(TAG, "Intentando obtener departamento del empleado directamente");
                getEmployeeInformation();
            }
        });
    }
    
    // Método alternativo para obtener información del empleado y su departamento
    private void getEmployeeInformation() {
        showLoading(true);
        
        apiClient.getEmployeeApiService().getAllEmployees().enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Employee> allEmployees = response.body();
                    
                    // Buscar el empleado que corresponde al usuario actual
                    for (Employee employee : allEmployees) {
                        if (employee.getUserId() != null && employee.getUserId().equals(userId)) {
                            Log.d(TAG, "Empleado encontrado por ID de usuario: " + employee.getId());
                            
                            // Obtener el departamento del empleado
                            departmentId = employee.getDepartmentId();
                            Log.d(TAG, "Departamento obtenido del empleado: " + departmentId);
                            
                            if (departmentId != null && !departmentId.isEmpty()) {
                                // Cargar los empleados del departamento
                                loadEmployeesForDepartment();
                                return;
                            }
                        }
                    }
                    
                    // Si no se encuentra el empleado o el departamento
                    Toast.makeText(getContext(), "No se pudo determinar el departamento", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error al obtener información del empleado", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error al obtener empleados: ", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadEmployeesForDepartment() {
        showLoading(true);
        
        // Cargar empleados del departamento del jefe
        apiClient.getEmployeeApiService().getAllEmployees(departmentId).enqueue(new Callback<List<Employee>>() {
            @Override
            public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    departmentEmployees = response.body();
                    Log.d(TAG, "Empleados cargados: " + departmentEmployees.size());
                    
                    // Cargar los turnos asignados
                    loadShiftAssignments();
                } else {
                    Toast.makeText(getContext(), "Error al cargar empleados", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<Employee>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error: ", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadData() {
        // Primero cargar los tipos de turno disponibles
        loadShiftTypes();
        
        if (isAdmin) {
            // Si es admin, cargar todas las asignaciones
            loadAllShiftAssignments();
        } else if (isDepartmentHead) {
            // Si es jefe, getDepartmentForManager() ya se habrá llamado
            // y ese método cargará los empleados y luego las asignaciones
        } else {
            // Si es empleado normal, cargar solo sus asignaciones
            loadEmployeeShiftAssignments();
        }
    }
    
    private void loadShiftTypes() {
        showLoading(true);
        
        apiClient.getShiftTypeApiService().getAllShiftTypes().enqueue(new Callback<List<ShiftType>>() {
            @Override
            public void onResponse(Call<List<ShiftType>> call, Response<List<ShiftType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableShiftTypes = response.body();
                    Log.d(TAG, "Tipos de turno cargados: " + availableShiftTypes.size());
                } else {
                    Toast.makeText(getContext(), "Error al cargar tipos de turno", Toast.LENGTH_SHORT).show();
                }
                
                showLoading(false);
            }
            
            @Override
            public void onFailure(Call<List<ShiftType>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error: ", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadAllShiftAssignments() {
        showLoading(true);
        
        apiClient.getShiftAssignmentApiService().getAllShiftAssignments().enqueue(new Callback<List<ShiftAssignment>>() {
            @Override
            public void onResponse(Call<List<ShiftAssignment>> call, Response<List<ShiftAssignment>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ShiftAssignment> assignments = response.body();
                    Log.d(TAG, "Asignaciones cargadas: " + assignments.size());
                    
                    // Actualizar UI con las asignaciones
                    updateUI(assignments);
                } else {
                    Toast.makeText(getContext(), "Error al cargar asignaciones", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<ShiftAssignment>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error: ", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadShiftAssignments() {
        showLoading(true);
        
        // Cargar asignaciones para el departamento del jefe
        apiClient.getShiftAssignmentApiService().getShiftAssignmentsByDepartment(departmentId).enqueue(new Callback<List<ShiftAssignment>>() {
            @Override
            public void onResponse(Call<List<ShiftAssignment>> call, Response<List<ShiftAssignment>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ShiftAssignment> assignments = response.body();
                    Log.d(TAG, "Asignaciones para el departamento: " + assignments.size());
                    
                    // Actualizar UI con las asignaciones
                    updateUI(assignments);
                } else {
                    Toast.makeText(getContext(), "Error al cargar asignaciones", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<ShiftAssignment>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error: ", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadEmployeeShiftAssignments() {
        showLoading(true);
        
        // Cargar asignaciones para el empleado actual
        apiClient.getShiftAssignmentApiService().getShiftAssignmentsByEmployee(userId).enqueue(new Callback<List<ShiftAssignment>>() {
            @Override
            public void onResponse(Call<List<ShiftAssignment>> call, Response<List<ShiftAssignment>> response) {
                showLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ShiftAssignment> assignments = response.body();
                    Log.d(TAG, "Asignaciones para el empleado: " + assignments.size());
                    
                    // Actualizar UI con las asignaciones
                    updateUI(assignments);
                } else {
                    Toast.makeText(getContext(), "Error al cargar asignaciones", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<List<ShiftAssignment>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error: ", t);
                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateUI(List<ShiftAssignment> assignments) {
        // Configurar el adaptador para el RecyclerView
        if (getContext() == null) return;
        
        // Crear mapas para referencias rápidas
        Map<String, Employee> employeeMap = new HashMap<>();
        Map<String, Department> departmentMap = new HashMap<>();
        Map<String, ShiftType> shiftTypeMap = new HashMap<>();
        
        // Cargar los tipos de turno
        for (ShiftType shiftType : availableShiftTypes) {
            shiftTypeMap.put(shiftType.getId(), shiftType);
        }
        
        // Cargar los empleados y departamentos
        // Para administradores, necesitamos cargar todos los empleados y departamentos
        if (isAdmin) {
            showLoading(true);
            // Cargar todos los empleados
            apiClient.getEmployeeApiService().getAllEmployees().enqueue(new Callback<List<Employee>>() {
                @Override
                public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Employee> employees = response.body();
                        for (Employee employee : employees) {
                            employeeMap.put(employee.getId(), employee);
                        }
                        
                        // Ahora cargar todos los departamentos
                        apiClient.getDepartmentApiService().getAllDepartments().enqueue(new Callback<List<Department>>() {
                            @Override
                            public void onResponse(Call<List<Department>> call, Response<List<Department>> response) {
                                showLoading(false);
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Department> departments = response.body();
                                    for (Department department : departments) {
                                        departmentMap.put(department.getId(), department);
                                    }
                                    
                                    // Finalmente, configurar el adaptador con todos los datos
                                    setupAdapter(assignments, employeeMap, departmentMap, shiftTypeMap);
                                } else {
                                    Toast.makeText(getContext(), "Error al cargar departamentos", Toast.LENGTH_SHORT).show();
                                }
                            }
                            
                            @Override
                            public void onFailure(Call<List<Department>> call, Throwable t) {
                                showLoading(false);
                                Log.e(TAG, "Error: ", t);
                                Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        showLoading(false);
                        Toast.makeText(getContext(), "Error al cargar empleados", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<List<Employee>> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Error: ", t);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (isDepartmentHead) {
            // Para jefes de departamento, solo necesitamos los empleados de su departamento
            // y su departamento
            for (Employee employee : departmentEmployees) {
                employeeMap.put(employee.getId(), employee);
            }
            
            // Cargar el departamento del jefe
            apiClient.getDepartmentApiService().getAllDepartments().enqueue(new Callback<List<Department>>() {
                @Override
                public void onResponse(Call<List<Department>> call, Response<List<Department>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Department> departments = response.body();
                        for (Department department : departments) {
                            departmentMap.put(department.getId(), department);
                        }
                        
                        // Configurar el adaptador con los datos disponibles
                        setupAdapter(assignments, employeeMap, departmentMap, shiftTypeMap);
                    } else {
                        Toast.makeText(getContext(), "Error al cargar departamentos", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<List<Department>> call, Throwable t) {
                    Log.e(TAG, "Error: ", t);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    
                    // Configurar el adaptador con los datos disponibles aunque falten los departamentos
                    setupAdapter(assignments, employeeMap, departmentMap, shiftTypeMap);
                }
            });
        } else {
            // Para empleados normales, solo necesitamos sus propias asignaciones
            // Obtener los datos del empleado
            apiClient.getEmployeeApiService().getAllEmployees().enqueue(new Callback<List<Employee>>() {
                @Override
                public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Employee> employees = response.body();
                        for (Employee employee : employees) {
                            employeeMap.put(employee.getId(), employee);
                        }
                        
                        // Configurar el adaptador con los datos disponibles
                        setupAdapter(assignments, employeeMap, departmentMap, shiftTypeMap);
                    } else {
                        Toast.makeText(getContext(), "Error al cargar empleados", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<List<Employee>> call, Throwable t) {
                    Log.e(TAG, "Error: ", t);
                    Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                    
                    // Configurar el adaptador con los datos disponibles aunque falten algunos
                    setupAdapter(assignments, employeeMap, departmentMap, shiftTypeMap);
                }
            });
        }
    }
    
    private void setupAdapter(List<ShiftAssignment> assignments, 
                             Map<String, Employee> employeeMap, 
                             Map<String, Department> departmentMap, 
                             Map<String, ShiftType> shiftTypeMap) {
        // Crear y configurar el adaptador                     
        ShiftAssignmentAdapter adapter = new ShiftAssignmentAdapter(
            assignments, 
            getContext(), 
            employeeMap, 
            departmentMap, 
            shiftTypeMap,
            new ShiftAssignmentAdapter.OnShiftAssignmentListener() {
                @Override
                public void onEditClick(ShiftAssignment shiftAssignment, int position) {
                    // Implementar edición de asignación
                    showEditShiftAssignmentDialog(shiftAssignment, position);
                }
                
                @Override
                public void onDeleteClick(ShiftAssignment shiftAssignment, int position) {
                    // Implementar eliminación de asignación
                    deleteShiftAssignment(shiftAssignment, position);
                }
            }
        );
        
        recyclerView.setAdapter(adapter);
    }
    
    private void deleteShiftAssignment(ShiftAssignment shiftAssignment, int position) {
        // Mostrar un diálogo de confirmación
        new AlertDialog.Builder(getContext())
            .setTitle("Eliminar asignación")
            .setMessage("¿Estás seguro de que deseas eliminar esta asignación de turno?")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                // Realizar la eliminación
                showLoading(true);
                apiClient.getShiftAssignmentApiService().deleteShiftAssignment(shiftAssignment.getId())
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            showLoading(false);
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Asignación eliminada", Toast.LENGTH_SHORT).show();
                                
                                // Recargar asignaciones según el rol
                                if (isAdmin) {
                                    loadAllShiftAssignments();
                                } else if (isDepartmentHead) {
                                    loadShiftAssignments();
                                } else {
                                    loadEmployeeShiftAssignments();
                                }
                            } else {
                                Toast.makeText(getContext(), "Error al eliminar la asignación", Toast.LENGTH_SHORT).show();
                            }
                        }
                        
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            showLoading(false);
                            Log.e(TAG, "Error: ", t);
                            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    private void showAddShiftAssignmentDialog() {
        // Inflar el layout del diálogo
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_shift_assignment, null);
        
        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Obtener referencias a las vistas del diálogo
        Spinner spinnerEmployee = dialogView.findViewById(R.id.spinnerEmployee);
        Spinner spinnerShiftType = dialogView.findViewById(R.id.spinnerShiftType);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        Button btnPickStartDate = dialogView.findViewById(R.id.btnPickStartDate);
        Button btnPickEndDate = dialogView.findViewById(R.id.btnPickEndDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        
        // Configurar adaptadores para los spinners
        // Empleados - dependiendo del rol mostramos todos o solo los del departamento
        List<Employee> employees = new ArrayList<>();
        if (isAdmin) {
            // Si es admin, obtener todos los empleados
            apiClient.getEmployeeApiService().getAllEmployees().enqueue(new Callback<List<Employee>>() {
                @Override
                public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        employees.addAll(response.body());
                        setupEmployeeSpinner(spinnerEmployee, employees);
                    }
                }
                
                @Override
                public void onFailure(Call<List<Employee>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error al cargar empleados", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (isDepartmentHead) {
            // Si es jefe de departamento, usar los empleados ya cargados
            if (departmentEmployees.isEmpty()) {
                // Si aún no se han cargado, cargarlos ahora
                loadEmployeesForDepartment();
            }
            setupEmployeeSpinner(spinnerEmployee, departmentEmployees);
        }
        
        // Tipos de turno
        if (availableShiftTypes.isEmpty()) {
            // Si aún no se han cargado, cargarlos ahora
            apiClient.getShiftTypeApiService().getAllShiftTypes().enqueue(new Callback<List<ShiftType>>() {
                @Override
                public void onResponse(Call<List<ShiftType>> call, Response<List<ShiftType>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        availableShiftTypes = response.body();
                        setupShiftTypeSpinner(spinnerShiftType, availableShiftTypes);
                    }
                }
                
                @Override
                public void onFailure(Call<List<ShiftType>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error al cargar tipos de turno", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            setupShiftTypeSpinner(spinnerShiftType, availableShiftTypes);
        }
        
        // Configurar pickers de fecha
        Calendar calendar = Calendar.getInstance();
        
        btnPickStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etStartDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        btnPickEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        etEndDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        // Botón cancelar
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Botón guardar
        btnSave.setOnClickListener(v -> {
            // Validar los campos
            if (spinnerEmployee.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                Toast.makeText(getContext(), "Selecciona un empleado", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (spinnerShiftType.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                Toast.makeText(getContext(), "Selecciona un tipo de turno", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String startDate = etStartDate.getText().toString();
            if (startDate.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona la fecha de inicio", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String endDate = etEndDate.getText().toString();
            if (endDate.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona la fecha de fin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Crear objeto ShiftAssignment
            Employee selectedEmployee = (Employee) spinnerEmployee.getSelectedItem();
            ShiftType selectedShiftType = (ShiftType) spinnerShiftType.getSelectedItem();
            
            // Convertir las cadenas de texto a objetos Date
            try {
                // Usar el formato dd/MM/yyyy para la visualización en el frontend
                SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                
                // Parsear las fechas para el modelo
                Date startDateObj = displayFormat.parse(startDate);
                Date endDateObj = displayFormat.parse(endDate);
                
                // Crear una versión del modelo ShiftAssignment adaptada para el backend
                ShiftAssignment newAssignment = new ShiftAssignment();
                newAssignment.setEmployeeId(selectedEmployee.getId());
                newAssignment.setShiftTypeId(selectedShiftType.getId());
                newAssignment.setStartDate(startDateObj);
                newAssignment.setEndDate(endDateObj);
                
                // Guardar la asignación
                saveShiftAssignment(newAssignment, dialog);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Error en el formato de fecha", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al parsear fechas: " + e.getMessage());
            }
        });
        
        dialog.show();
    }
    
    private void setupEmployeeSpinner(Spinner spinner, List<Employee> employees) {
        if (getContext() == null) return;
        
        // Crear adaptador personalizado
        ArrayAdapter<Employee> adapter = new ArrayAdapter<Employee>(
                getContext(),
                android.R.layout.simple_spinner_item,
                employees
        ) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view;
                Employee employee = getItem(position);
                if (employee != null) {
                    text.setText(employee.getFirstName() + " " + employee.getLastName());
                }
                return view;
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view;
                Employee employee = getItem(position);
                if (employee != null) {
                    text.setText(employee.getFirstName() + " " + employee.getLastName());
                }
                return view;
            }
        };
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void setupShiftTypeSpinner(Spinner spinner, List<ShiftType> shiftTypes) {
        if (getContext() == null) return;
        
        // Crear adaptador personalizado
        ArrayAdapter<ShiftType> adapter = new ArrayAdapter<ShiftType>(
                getContext(),
                android.R.layout.simple_spinner_item,
                shiftTypes
        ) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view;
                ShiftType shiftType = getItem(position);
                if (shiftType != null) {
                    text.setText(shiftType.getName());
                }
                return view;
            }
            
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view;
                ShiftType shiftType = getItem(position);
                if (shiftType != null) {
                    text.setText(shiftType.getName());
                }
                return view;
            }
        };
        
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
    private void saveShiftAssignment(ShiftAssignment assignment, AlertDialog dialog) {
        showLoading(true);
        
        try {
            // Crear una copia del objeto con las fechas formateadas correctamente para el backend
            // El backend usa LocalDate, así que debemos enviar las fechas en el formato ISO (yyyy-MM-dd)
            SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            // Crear un mapa para enviar los datos con el formato correcto
            Map<String, Object> assignmentMap = new HashMap<>();
            assignmentMap.put("employeeId", assignment.getEmployeeId());
            assignmentMap.put("shiftTypeId", assignment.getShiftTypeId());
            assignmentMap.put("startDate", isoDateFormat.format(assignment.getStartDate()));
            assignmentMap.put("endDate", isoDateFormat.format(assignment.getEndDate()));
            
            // Agregar logs para depurar
            Log.d(TAG, "Enviando asignación de turno:");
            Log.d(TAG, "  - EmployeeId: " + assignmentMap.get("employeeId"));
            Log.d(TAG, "  - ShiftTypeId: " + assignmentMap.get("shiftTypeId"));
            Log.d(TAG, "  - StartDate: " + assignmentMap.get("startDate"));
            Log.d(TAG, "  - EndDate: " + assignmentMap.get("endDate"));
            
            apiClient.getShiftAssignmentApiService().createShiftAssignmentWithMap(assignmentMap).enqueue(new Callback<ShiftAssignment>() {
                @Override
                public void onResponse(Call<ShiftAssignment> call, Response<ShiftAssignment> response) {
                    showLoading(false);
                    
                    // Agregar logs para depurar la respuesta
                    Log.d(TAG, "Respuesta del servidor - Código: " + response.code());
                    if (!response.isSuccessful()) {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No hay cuerpo de error";
                            Log.e(TAG, "Error en la respuesta: " + errorBody);
                            // Mostrar un mensaje más específico al usuario
                            if (response.code() == 403) {
                                Toast.makeText(getContext(), "No tienes permisos para crear esta asignación", Toast.LENGTH_LONG).show();
                            } else if (response.code() == 400) {
                                Toast.makeText(getContext(), "Datos incorrectos: " + errorBody, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Error al crear la asignación: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "No se pudo leer el error: " + e.getMessage());
                            Toast.makeText(getContext(), "Error al crear la asignación", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    
                    if (response.body() != null) {
                        Log.d(TAG, "Asignación creada con ID: " + response.body().getId());
                        Toast.makeText(getContext(), "Asignación creada con éxito", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        
                        // Recargar las asignaciones
                        if (isAdmin) {
                            loadAllShiftAssignments();
                        } else if (isDepartmentHead) {
                            loadShiftAssignments();
                        } else {
                            loadEmployeeShiftAssignments();
                        }
                    } else {
                        Log.e(TAG, "Respuesta exitosa pero cuerpo vacío");
                        Toast.makeText(getContext(), "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ShiftAssignment> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Error de conexión: ", t);
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error al preparar la asignación: ", e);
            Toast.makeText(getContext(), "Error al preparar los datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showEditShiftAssignmentDialog(ShiftAssignment currentAssignment, int position) {
        // Inflar el layout del diálogo
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_shift_assignment, null);
        
        // Crear el AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Editar Asignación de Turno");
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        
        // Obtener referencias a las vistas del diálogo
        Spinner spinnerEmployee = dialogView.findViewById(R.id.spinnerEmployee);
        Spinner spinnerShiftType = dialogView.findViewById(R.id.spinnerShiftType);
        EditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        EditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        Button btnPickStartDate = dialogView.findViewById(R.id.btnPickStartDate);
        Button btnPickEndDate = dialogView.findViewById(R.id.btnPickEndDate);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        
        // Cambiar el texto del botón guardar
        btnSave.setText("Actualizar");
        
        // Configurar adaptadores para los spinners
        List<Employee> employees = new ArrayList<>();
        if (isAdmin) {
            // Si es admin, obtener todos los empleados
            apiClient.getEmployeeApiService().getAllEmployees().enqueue(new Callback<List<Employee>>() {
                @Override
                public void onResponse(Call<List<Employee>> call, Response<List<Employee>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        employees.addAll(response.body());
                        setupEmployeeSpinner(spinnerEmployee, employees);
                        
                        // Preseleccionar el empleado actual
                        for (int i = 0; i < employees.size(); i++) {
                            if (employees.get(i).getId().equals(currentAssignment.getEmployeeId())) {
                                spinnerEmployee.setSelection(i);
                                break;
                            }
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<List<Employee>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error al cargar empleados", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (isDepartmentHead) {
            // Si es jefe de departamento, usar los empleados ya cargados
            if (departmentEmployees.isEmpty()) {
                loadEmployeesForDepartment();
            }
            setupEmployeeSpinner(spinnerEmployee, departmentEmployees);
            
            // Preseleccionar el empleado actual
            for (int i = 0; i < departmentEmployees.size(); i++) {
                if (departmentEmployees.get(i).getId().equals(currentAssignment.getEmployeeId())) {
                    spinnerEmployee.setSelection(i);
                    break;
                }
            }
        }
        
        // Tipos de turno
        if (availableShiftTypes.isEmpty()) {
            apiClient.getShiftTypeApiService().getAllShiftTypes().enqueue(new Callback<List<ShiftType>>() {
                @Override
                public void onResponse(Call<List<ShiftType>> call, Response<List<ShiftType>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        availableShiftTypes = response.body();
                        setupShiftTypeSpinner(spinnerShiftType, availableShiftTypes);
                        
                        // Preseleccionar el tipo de turno actual
                        for (int i = 0; i < availableShiftTypes.size(); i++) {
                            if (availableShiftTypes.get(i).getId().equals(currentAssignment.getShiftTypeId())) {
                                spinnerShiftType.setSelection(i);
                                break;
                            }
                        }
                    }
                }
                
                @Override
                public void onFailure(Call<List<ShiftType>> call, Throwable t) {
                    Toast.makeText(getContext(), "Error al cargar tipos de turno", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            setupShiftTypeSpinner(spinnerShiftType, availableShiftTypes);
            
            // Preseleccionar el tipo de turno actual
            for (int i = 0; i < availableShiftTypes.size(); i++) {
                if (availableShiftTypes.get(i).getId().equals(currentAssignment.getShiftTypeId())) {
                    spinnerShiftType.setSelection(i);
                    break;
                }
            }
        }
        
        // Precargar las fechas actuales
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        if (currentAssignment.getStartDate() != null) {
            etStartDate.setText(displayFormat.format(currentAssignment.getStartDate()));
        }
        if (currentAssignment.getEndDate() != null) {
            etEndDate.setText(displayFormat.format(currentAssignment.getEndDate()));
        }
        
        // Configurar pickers de fecha
        Calendar calendar = Calendar.getInstance();
        
        btnPickStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etStartDate.setText(displayFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        btnPickEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    getContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etEndDate.setText(displayFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
        
        // Botón cancelar
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Botón actualizar
        btnSave.setOnClickListener(v -> {
            // Validar los campos
            if (spinnerEmployee.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                Toast.makeText(getContext(), "Selecciona un empleado", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (spinnerShiftType.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                Toast.makeText(getContext(), "Selecciona un tipo de turno", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String startDate = etStartDate.getText().toString();
            if (startDate.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona la fecha de inicio", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String endDate = etEndDate.getText().toString();
            if (endDate.isEmpty()) {
                Toast.makeText(getContext(), "Selecciona la fecha de fin", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Crear objeto ShiftAssignment actualizado
            Employee selectedEmployee = (Employee) spinnerEmployee.getSelectedItem();
            ShiftType selectedShiftType = (ShiftType) spinnerShiftType.getSelectedItem();
            
            // Convertir las cadenas de texto a objetos Date
            try {
                Date startDateObj = displayFormat.parse(startDate);
                Date endDateObj = displayFormat.parse(endDate);
                
                // Crear una versión actualizada del modelo ShiftAssignment
                ShiftAssignment updatedAssignment = new ShiftAssignment();
                updatedAssignment.setId(currentAssignment.getId()); // Mantener el ID original
                updatedAssignment.setEmployeeId(selectedEmployee.getId());
                updatedAssignment.setShiftTypeId(selectedShiftType.getId());
                updatedAssignment.setStartDate(startDateObj);
                updatedAssignment.setEndDate(endDateObj);
                
                // Actualizar la asignación
                updateShiftAssignment(updatedAssignment, dialog);
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Error en el formato de fecha", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error al parsear fechas: " + e.getMessage());
            }
        });
        
        dialog.show();
    }
    
    private void updateShiftAssignment(ShiftAssignment assignment, AlertDialog dialog) {
        showLoading(true);
        
        try {
            // Crear un mapa para enviar los datos con el formato correcto
            SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Map<String, Object> assignmentMap = new HashMap<>();
            assignmentMap.put("employeeId", assignment.getEmployeeId());
            assignmentMap.put("shiftTypeId", assignment.getShiftTypeId());
            assignmentMap.put("startDate", isoDateFormat.format(assignment.getStartDate()));
            assignmentMap.put("endDate", isoDateFormat.format(assignment.getEndDate()));
            
            Log.d(TAG, "Actualizando asignación de turno con ID: " + assignment.getId());
            Log.d(TAG, "  - EmployeeId: " + assignmentMap.get("employeeId"));
            Log.d(TAG, "  - ShiftTypeId: " + assignmentMap.get("shiftTypeId"));
            Log.d(TAG, "  - StartDate: " + assignmentMap.get("startDate"));
            Log.d(TAG, "  - EndDate: " + assignmentMap.get("endDate"));
            
            // Crear un objeto ShiftAssignment temporal para la actualización
            ShiftAssignment tempAssignment = new ShiftAssignment();
            tempAssignment.setEmployeeId(assignment.getEmployeeId());
            tempAssignment.setShiftTypeId(assignment.getShiftTypeId());
            tempAssignment.setStartDate(assignment.getStartDate());
            tempAssignment.setEndDate(assignment.getEndDate());
            
            apiClient.getShiftAssignmentApiService().updateShiftAssignment(assignment.getId(), tempAssignment).enqueue(new Callback<ShiftAssignment>() {
                @Override
                public void onResponse(Call<ShiftAssignment> call, Response<ShiftAssignment> response) {
                    showLoading(false);
                    
                    Log.d(TAG, "Respuesta del servidor - Código: " + response.code());
                    if (!response.isSuccessful()) {
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No hay cuerpo de error";
                            Log.e(TAG, "Error en la respuesta: " + errorBody);
                            if (response.code() == 403) {
                                Toast.makeText(getContext(), "No tienes permisos para actualizar esta asignación", Toast.LENGTH_LONG).show();
                            } else if (response.code() == 400) {
                                Toast.makeText(getContext(), "Datos incorrectos: " + errorBody, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getContext(), "Error al actualizar la asignación: " + response.code(), Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "No se pudo leer el error: " + e.getMessage());
                            Toast.makeText(getContext(), "Error al actualizar la asignación", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
                    
                    if (response.body() != null) {
                        Log.d(TAG, "Asignación actualizada con ID: " + response.body().getId());
                        Toast.makeText(getContext(), "Asignación actualizada con éxito", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        
                        // Recargar las asignaciones
                        if (isAdmin) {
                            loadAllShiftAssignments();
                        } else if (isDepartmentHead) {
                            loadShiftAssignments();
                        } else {
                            loadEmployeeShiftAssignments();
                        }
                    } else {
                        Log.e(TAG, "Respuesta exitosa pero cuerpo vacío");
                        Toast.makeText(getContext(), "Respuesta vacía del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
                
                @Override
                public void onFailure(Call<ShiftAssignment> call, Throwable t) {
                    showLoading(false);
                    Log.e(TAG, "Error de conexión: ", t);
                    Toast.makeText(getContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error al preparar la actualización: ", e);
            Toast.makeText(getContext(), "Error al preparar los datos: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
    }
} 