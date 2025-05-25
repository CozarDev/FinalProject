package com.proyectofinal.frontend.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.proyectofinal.frontend.Models.Incidence;
import com.proyectofinal.frontend.R;
import com.proyectofinal.frontend.Services.IncidenceApiService;
import com.proyectofinal.frontend.Api.ApiClient;

public class CreateIncidenceDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = "CreateIncidenceDialog";
    private static final String ARG_IS_ADMIN = "is_admin";
    private static final String ARG_CURRENT_USER_ID = "current_user_id";

    private TextInputLayout titleInputLayout;
    private TextInputEditText titleEditText;
    private TextInputLayout descriptionInputLayout;
    private TextInputEditText descriptionEditText;
    private TextInputLayout priorityInputLayout;
    private AutoCompleteTextView priorityDropdown;
    private TextInputLayout createdByInputLayout;
    private TextInputEditText createdByEditText;
    private MaterialButton btnCancel;
    private MaterialButton btnCreate;

    private boolean isAdmin;
    private String currentUserId;
    private IncidenceApiService incidenceApiService;
    private OnIncidenceCreatedListener listener;

    // Interface para callback
    public interface OnIncidenceCreatedListener {
        void onIncidenceCreated(Incidence incidence);
    }

    public static CreateIncidenceDialogFragment newInstance(boolean isAdmin, String currentUserId) {
        CreateIncidenceDialogFragment fragment = new CreateIncidenceDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ADMIN, isAdmin);
        args.putString(ARG_CURRENT_USER_ID, currentUserId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            isAdmin = getArguments().getBoolean(ARG_IS_ADMIN, false);
            currentUserId = getArguments().getString(ARG_CURRENT_USER_ID, "");
        }

        // Inicializar API Service
        ApiClient apiClient = new ApiClient(requireContext());
        incidenceApiService = new IncidenceApiService(apiClient);

        Log.d(TAG, "Diálogo creado - Admin: " + isAdmin + ", UserID: " + currentUserId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_create_incidence, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupPriorityDropdown();
        setupAdminFields();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        titleInputLayout = view.findViewById(R.id.titleInputLayout);
        titleEditText = view.findViewById(R.id.titleEditText);
        descriptionInputLayout = view.findViewById(R.id.descriptionInputLayout);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        priorityInputLayout = view.findViewById(R.id.priorityInputLayout);
        priorityDropdown = view.findViewById(R.id.priorityDropdown);
        createdByInputLayout = view.findViewById(R.id.createdByInputLayout);
        createdByEditText = view.findViewById(R.id.createdByEditText);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnCreate = view.findViewById(R.id.btnCreate);
    }

    private void setupPriorityDropdown() {
        String[] priorities = {"Alta", "Media", "Baja"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_dropdown_item_1line, 
            priorities
        );
        priorityDropdown.setAdapter(adapter);
        
        // Seleccionar "Media" por defecto
        priorityDropdown.setText("Media", false);
    }

    private void setupAdminFields() {
        if (isAdmin) {
            // Los admins pueden especificar quién crea la incidencia
            createdByInputLayout.setVisibility(View.VISIBLE);
            createdByEditText.setText(currentUserId);
        } else {
            // Los usuarios normales solo pueden crear para sí mismos
            createdByInputLayout.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> dismiss());
        btnCreate.setOnClickListener(v -> createIncidence());
    }

    private void createIncidence() {
        // Validar entrada
        if (!validateInput()) {
            return;
        }

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priorityText = priorityDropdown.getText().toString();
        String createdBy = isAdmin ? createdByEditText.getText().toString().trim() : currentUserId;

        // Convertir texto de prioridad a enum
        Incidence.Priority priority = convertPriorityFromText(priorityText);

        // Deshabilitar botón mientras se crea
        btnCreate.setEnabled(false);
        btnCreate.setText("Creando...");

        incidenceApiService.createIncidence(title, description, priority, isAdmin, createdBy, 
            new IncidenceApiService.IncidenceCallback() {
                @Override
                public void onSuccess(Incidence incidence) {
                    Log.d(TAG, "Incidencia creada exitosamente: " + incidence.getId());
                    Toast.makeText(requireContext(), "Incidencia creada exitosamente", Toast.LENGTH_SHORT).show();
                    
                    // Notificar al listener
                    if (listener != null) {
                        listener.onIncidenceCreated(incidence);
                    }
                    
                    dismiss();
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error creando incidencia: " + error);
                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                    
                    // Rehabilitar botón
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Crear Incidencia");
                }
            });
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Validar título
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            titleInputLayout.setError("El título es obligatorio");
            isValid = false;
        } else if (title.length() < 3) {
            titleInputLayout.setError("El título debe tener al menos 3 caracteres");
            isValid = false;
        } else if (title.length() > 100) {
            titleInputLayout.setError("El título no puede exceder 100 caracteres");
            isValid = false;
        } else {
            titleInputLayout.setError(null);
        }

        // Validar descripción
        String description = descriptionEditText.getText().toString().trim();
        if (description.isEmpty()) {
            descriptionInputLayout.setError("La descripción es obligatoria");
            isValid = false;
        } else if (description.length() < 10) {
            descriptionInputLayout.setError("La descripción debe tener al menos 10 caracteres");
            isValid = false;
        } else if (description.length() > 1000) {
            descriptionInputLayout.setError("La descripción no puede exceder 1000 caracteres");
            isValid = false;
        } else {
            descriptionInputLayout.setError(null);
        }

        // Validar prioridad
        String priority = priorityDropdown.getText().toString();
        if (priority.isEmpty() || (!priority.equals("Alta") && !priority.equals("Media") && !priority.equals("Baja"))) {
            priorityInputLayout.setError("Selecciona una prioridad válida");
            isValid = false;
        } else {
            priorityInputLayout.setError(null);
        }

        // Validar createdBy para admin
        if (isAdmin) {
            String createdBy = createdByEditText.getText().toString().trim();
            if (createdBy.isEmpty()) {
                createdByInputLayout.setError("El ID del creador es obligatorio");
                isValid = false;
            } else {
                createdByInputLayout.setError(null);
            }
        }

        return isValid;
    }

    private Incidence.Priority convertPriorityFromText(String priorityText) {
        switch (priorityText) {
            case "Alta":
                return Incidence.Priority.ALTA;
            case "Media":
                return Incidence.Priority.MEDIA;
            case "Baja":
                return Incidence.Priority.BAJA;
            default:
                return Incidence.Priority.MEDIA; // Fallback
        }
    }

    public void setOnIncidenceCreatedListener(OnIncidenceCreatedListener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }
} 