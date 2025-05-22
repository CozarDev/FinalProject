package com.proyectofinal.frontend.Fragments;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.proyectofinal.frontend.Adapters.ShiftTypeAdapter;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.ShiftTypeApiService;
import com.proyectofinal.frontend.Models.ShiftType;
import com.proyectofinal.frontend.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageShiftTypesFragment extends Fragment implements ShiftTypeAdapter.OnShiftTypeClickListener {

    private static final String TAG = "ManageShiftTypesFragment";
    private Context context;
    private ApiClient apiClient;
    private ShiftTypeApiService shiftTypeApiService;
    private ShiftTypeAdapter adapter;

    // Vistas de la lista
    private RecyclerView recyclerView;
    private TextView emptyStateTextView;
    private ProgressBar loadingProgressBar;
    private FloatingActionButton addButton;
    private ConstraintLayout listContainer;
    
    // Vistas del formulario
    private ConstraintLayout formContainer;
    private TextView formTitleTextView;
    private TextInputEditText nameEditText, startTimeEditText, endTimeEditText;
    private CheckBox mondayCheckBox, tuesdayCheckBox, wednesdayCheckBox, thursdayCheckBox, 
                     fridayCheckBox, saturdayCheckBox, sundayCheckBox;
    private RadioGroup colorRadioGroup;
    private Button saveButton, cancelButton;
    
    // Mapa de colores predefinidos
    private Map<Integer, String> colorMap = new HashMap<>();
    
    // Estado
    private ShiftType currentShiftType;
    private boolean isEditing = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_shift_types, container, false);
        
        // Inicializar ApiClient
        apiClient = ApiClient.getInstance(context);
        shiftTypeApiService = apiClient.getShiftTypeApiService();
        
        // Inicializar mapa de colores
        initColorMap();
        
        // Inicializar vistas de la lista
        recyclerView = view.findViewById(R.id.shiftTypesRecyclerView);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        addButton = view.findViewById(R.id.addShiftTypeButton);
        listContainer = view.findViewById(R.id.listContainer);
        
        // Inicializar vistas del formulario
        formContainer = view.findViewById(R.id.formContainer);
        formTitleTextView = view.findViewById(R.id.formTitleTextView);
        nameEditText = view.findViewById(R.id.nameEditText);
        startTimeEditText = view.findViewById(R.id.startTimeEditText);
        endTimeEditText = view.findViewById(R.id.endTimeEditText);
        colorRadioGroup = view.findViewById(R.id.colorRadioGroup);
        mondayCheckBox = view.findViewById(R.id.mondayCheckBox);
        tuesdayCheckBox = view.findViewById(R.id.tuesdayCheckBox);
        wednesdayCheckBox = view.findViewById(R.id.wednesdayCheckBox);
        thursdayCheckBox = view.findViewById(R.id.thursdayCheckBox);
        fridayCheckBox = view.findViewById(R.id.fridayCheckBox);
        saturdayCheckBox = view.findViewById(R.id.saturdayCheckBox);
        sundayCheckBox = view.findViewById(R.id.sundayCheckBox);
        saveButton = view.findViewById(R.id.saveShiftTypeButton);
        cancelButton = view.findViewById(R.id.cancelShiftTypeButton);
        
        // Configurar RecyclerView
        adapter = new ShiftTypeAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        
        // Configurar eventos
        addButton.setOnClickListener(v -> showAddForm());
        cancelButton.setOnClickListener(v -> showListView());
        saveButton.setOnClickListener(v -> validateAndSaveShiftType());
        
        // Configurar selección de hora
        setupTimeSelectors();
        
        // Cargar datos
        loadShiftTypes();
        
        return view;
    }
    
    private void initColorMap() {
        colorMap.put(R.id.blueRadioButton, "#2196F3"); // Azul
        colorMap.put(R.id.greenRadioButton, "#4CAF50"); // Verde
        colorMap.put(R.id.redRadioButton, "#F44336"); // Rojo
        colorMap.put(R.id.yellowRadioButton, "#FFEB3B"); // Amarillo
        colorMap.put(R.id.purpleRadioButton, "#9C27B0"); // Morado
    }
    
    private void setupTimeSelectors() {
        startTimeEditText.setOnClickListener(v -> showTimePickerDialog(startTimeEditText));
        endTimeEditText.setOnClickListener(v -> showTimePickerDialog(endTimeEditText));
    }
    
    private void showTimePickerDialog(final TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                context,
                (view, hourOfDay, minutes) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minutes);
                    editText.setText(time);
                },
                hour,
                minute,
                true);
        
        timePickerDialog.show();
    }
    
    private void loadShiftTypes() {
        setLoading(true);
        
        Log.d(TAG, "Cargando tipos de turno...");
        shiftTypeApiService.getAllShiftTypes().enqueue(new Callback<List<ShiftType>>() {
            @Override
            public void onResponse(Call<List<ShiftType>> call, Response<List<ShiftType>> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<ShiftType> shiftTypes = response.body();
                    Log.d(TAG, "Tipos de turno cargados: " + shiftTypes.size());
                    adapter.setShiftTypes(shiftTypes);
                    
                    if (shiftTypes.isEmpty()) {
                        showEmptyState(true);
                    } else {
                        showEmptyState(false);
                    }
                } else {
                    int statusCode = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    
                    Log.e(TAG, "Error HTTP " + statusCode + ": " + errorBody);
                    showError("Error al cargar los tipos de turno (HTTP " + statusCode + "): " + 
                             (response.message() != null ? response.message() : "Error desconocido"));
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<List<ShiftType>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Error de conexión", t);
                showError("Error de conexión: " + t.getMessage());
                showEmptyState(true);
            }
        });
    }
    
    private void setLoading(boolean isLoading) {
        loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState(boolean isEmpty) {
        emptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
    
    private void showAddForm() {
        currentShiftType = new ShiftType();
        isEditing = false;
        formTitleTextView.setText("Nuevo Tipo de Turno");
        clearForm();
        
        // Predeterminar horario laboral de lunes a viernes
        mondayCheckBox.setChecked(true);
        tuesdayCheckBox.setChecked(true);
        wednesdayCheckBox.setChecked(true);
        thursdayCheckBox.setChecked(true);
        fridayCheckBox.setChecked(true);
        saturdayCheckBox.setChecked(false);
        sundayCheckBox.setChecked(false);
        
        // Predeterminar color azul (primer RadioButton)
        colorRadioGroup.check(R.id.blueRadioButton);
        
        // Mostrar formulario
        listContainer.setVisibility(View.GONE);
        formContainer.setVisibility(View.VISIBLE);
    }
    
    private void showEditForm(ShiftType shiftType) {
        currentShiftType = shiftType;
        isEditing = true;
        formTitleTextView.setText("Editar Tipo de Turno");
        
        // Rellenar formulario con datos del turno
        nameEditText.setText(shiftType.getName());
        startTimeEditText.setText(shiftType.getStartTime());
        endTimeEditText.setText(shiftType.getEndTime());
        
        // Seleccionar RadioButton según el color
        selectRadioButtonByColor(shiftType.getColor());
        
        // Marcar días de la semana
        boolean[] workDays = shiftType.getWorkDays();
        if (workDays != null && workDays.length == 7) {
            mondayCheckBox.setChecked(workDays[0]);
            tuesdayCheckBox.setChecked(workDays[1]);
            wednesdayCheckBox.setChecked(workDays[2]);
            thursdayCheckBox.setChecked(workDays[3]);
            fridayCheckBox.setChecked(workDays[4]);
            saturdayCheckBox.setChecked(workDays[5]);
            sundayCheckBox.setChecked(workDays[6]);
        }
        
        // Mostrar formulario
        listContainer.setVisibility(View.GONE);
        formContainer.setVisibility(View.VISIBLE);
    }
    
    private void selectRadioButtonByColor(String color) {
        // Por defecto, seleccionar el azul
        int selectedId = R.id.blueRadioButton;
        
        // Buscar el RadioButton correspondiente al color
        for (Map.Entry<Integer, String> entry : colorMap.entrySet()) {
            if (entry.getValue().equals(color)) {
                selectedId = entry.getKey();
                break;
            }
        }
        
        colorRadioGroup.check(selectedId);
    }
    
    private void showListView() {
        listContainer.setVisibility(View.VISIBLE);
        formContainer.setVisibility(View.GONE);
    }
    
    private void clearForm() {
        nameEditText.setText("");
        startTimeEditText.setText("");
        endTimeEditText.setText("");
        colorRadioGroup.clearCheck();
        mondayCheckBox.setChecked(false);
        tuesdayCheckBox.setChecked(false);
        wednesdayCheckBox.setChecked(false);
        thursdayCheckBox.setChecked(false);
        fridayCheckBox.setChecked(false);
        saturdayCheckBox.setChecked(false);
        sundayCheckBox.setChecked(false);
    }
    
    private void validateAndSaveShiftType() {
        String name = nameEditText.getText().toString().trim();
        String startTime = startTimeEditText.getText().toString().trim();
        String endTime = endTimeEditText.getText().toString().trim();
        
        // Obtener color seleccionado
        int selectedColorId = colorRadioGroup.getCheckedRadioButtonId();
        String color = colorMap.getOrDefault(selectedColorId, "#2196F3"); // Azul por defecto
        
        // Validaciones
        if (name.isEmpty()) {
            nameEditText.setError("El nombre es obligatorio");
            return;
        }
        
        if (startTime.isEmpty()) {
            startTimeEditText.setError("La hora de inicio es obligatoria");
            return;
        }
        
        if (endTime.isEmpty()) {
            endTimeEditText.setError("La hora de fin es obligatoria");
            return;
        }
        
        if (selectedColorId == -1) {
            showError("Debe seleccionar un color");
            return;
        }
        
        // Obtener días de trabajo
        boolean[] workDays = new boolean[7];
        workDays[0] = mondayCheckBox.isChecked();
        workDays[1] = tuesdayCheckBox.isChecked();
        workDays[2] = wednesdayCheckBox.isChecked();
        workDays[3] = thursdayCheckBox.isChecked();
        workDays[4] = fridayCheckBox.isChecked();
        workDays[5] = saturdayCheckBox.isChecked();
        workDays[6] = sundayCheckBox.isChecked();
        
        // Al menos un día debe estar seleccionado
        boolean anyDaySelected = false;
        for (boolean day : workDays) {
            if (day) {
                anyDaySelected = true;
                break;
            }
        }
        
        if (!anyDaySelected) {
            showError("Debe seleccionar al menos un día de la semana");
            return;
        }
        
        // Actualizar objeto ShiftType
        currentShiftType.setName(name);
        currentShiftType.setStartTime(startTime);
        currentShiftType.setEndTime(endTime);
        currentShiftType.setWorkDays(workDays);
        currentShiftType.setColor(color);
        
        // Mostrar progreso mientras se guarda
        setLoading(true);
        
        // Guardar en el servidor
        if (isEditing) {
            updateShiftType(currentShiftType);
        } else {
            createShiftType(currentShiftType);
        }
    }
    
    private void createShiftType(ShiftType shiftType) {
        Log.d(TAG, "Creando tipo de turno: " + shiftType.getName());
        
        shiftTypeApiService.createShiftType(shiftType).enqueue(new Callback<ShiftType>() {
            @Override
            public void onResponse(Call<ShiftType> call, Response<ShiftType> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ShiftType createdShiftType = response.body();
                    Log.d(TAG, "Tipo de turno creado con ID: " + createdShiftType.getId());
                    adapter.addShiftType(createdShiftType);
                    showEmptyState(false);
                    showListView();
                    showError("Tipo de turno creado correctamente");
                } else {
                    int statusCode = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    
                    Log.e(TAG, "Error HTTP " + statusCode + ": " + errorBody);
                    showError("Error al crear el tipo de turno (HTTP " + statusCode + "): " + 
                             (response.message() != null ? response.message() : "Error desconocido"));
                }
            }

            @Override
            public void onFailure(Call<ShiftType> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Error de conexión al crear tipo de turno", t);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    private void updateShiftType(ShiftType shiftType) {
        Log.d(TAG, "Actualizando tipo de turno con ID: " + shiftType.getId());
        
        shiftTypeApiService.updateShiftType(shiftType.getId(), shiftType).enqueue(new Callback<ShiftType>() {
            @Override
            public void onResponse(Call<ShiftType> call, Response<ShiftType> response) {
                setLoading(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    ShiftType updatedShiftType = response.body();
                    Log.d(TAG, "Tipo de turno actualizado: " + updatedShiftType.getName());
                    adapter.updateShiftType(updatedShiftType);
                    showListView();
                    showError("Tipo de turno actualizado correctamente");
                } else {
                    int statusCode = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error al leer errorBody", e);
                    }
                    
                    Log.e(TAG, "Error HTTP " + statusCode + ": " + errorBody);
                    showError("Error al actualizar el tipo de turno (HTTP " + statusCode + "): " + 
                             (response.message() != null ? response.message() : "Error desconocido"));
                }
            }

            @Override
            public void onFailure(Call<ShiftType> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Error de conexión al actualizar tipo de turno", t);
                showError("Error de conexión: " + t.getMessage());
            }
        });
    }
    
    private void deleteShiftType(ShiftType shiftType) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Eliminar tipo de turno")
                .setMessage("¿Está seguro de que desea eliminar este tipo de turno? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    setLoading(true);
                    Log.d(TAG, "Eliminando tipo de turno con ID: " + shiftType.getId());
                    
                    shiftTypeApiService.deleteShiftType(shiftType.getId()).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            setLoading(false);
                            
                            if (response.isSuccessful()) {
                                Log.d(TAG, "Tipo de turno eliminado correctamente");
                                adapter.removeShiftType(shiftType.getId());
                                if (adapter.getItemCount() == 0) {
                                    showEmptyState(true);
                                }
                                showError("Tipo de turno eliminado correctamente");
                            } else {
                                int statusCode = response.code();
                                String errorBody = "";
                                try {
                                    if (response.errorBody() != null) {
                                        errorBody = response.errorBody().string();
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error al leer errorBody", e);
                                }
                                
                                Log.e(TAG, "Error HTTP " + statusCode + ": " + errorBody);
                                showError("Error al eliminar el tipo de turno (HTTP " + statusCode + "): " + 
                                         (response.message() != null ? response.message() : "Error desconocido"));
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            setLoading(false);
                            Log.e(TAG, "Error de conexión al eliminar tipo de turno", t);
                            showError("Error de conexión: " + t.getMessage());
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditShiftType(ShiftType shiftType, int position) {
        showEditForm(shiftType);
    }

    @Override
    public void onDeleteShiftType(ShiftType shiftType, int position) {
        deleteShiftType(shiftType);
    }
} 