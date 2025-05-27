package com.proyectofinal.frontend.Fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Api.UserApiService;
import com.proyectofinal.frontend.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class UserProfileFragment extends Fragment {

    private TextInputEditText firstNameEditText, lastNameEditText, emailEditText, phoneEditText, usernameEditText, passwordEditText;
    private TextView rolTextView;
    private View buttonsContainer;
    private MaterialButton updateButton, cancelButton;
    private UserApiService userApiService;
    private String token;
    private Map<String, Object> originalProfileData;
    private boolean isDataModified = false;
    private boolean isPasswordModified = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        
        // Configurar Toolbar con botón de retroceso
        Toolbar toolbar = requireActivity().findViewById(R.id.materialToolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        
        // Inicializar vistas
        initializeViews(view);
        
        // Configurar listeners
        setupTextChangeListeners();
        setupButtonListeners();
        
        // Obtener token de las preferencias
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        token = prefs.getString("JWT_TOKEN", "");
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "Error: No se pudo obtener la sesión del usuario", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
            return view;
        }
        
        // Inicializar ApiClient y servicio
        userApiService = ApiClient.getInstance(requireContext()).getUserApiService();
        
        // Cargar datos del perfil
        loadUserProfile();
        
        return view;
    }
    
    private void initializeViews(View view) {
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        rolTextView = view.findViewById(R.id.rolTextView);
        
        buttonsContainer = view.findViewById(R.id.buttonsContainer);
        updateButton = view.findViewById(R.id.updateButton);
        cancelButton = view.findViewById(R.id.cancelButton);
    }
    
    private void setupTextChangeListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                checkForChanges();
            }
        };
        
        firstNameEditText.addTextChangedListener(textWatcher);
        lastNameEditText.addTextChangedListener(textWatcher);
        emailEditText.addTextChangedListener(textWatcher);
        phoneEditText.addTextChangedListener(textWatcher);
        usernameEditText.addTextChangedListener(textWatcher);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isPasswordModified = true;
                checkForChanges();
            }
        });
    }
    
    private void setupButtonListeners() {
        updateButton.setOnClickListener(v -> updateProfile());
        cancelButton.setOnClickListener(v -> resetFields());
    }
    
    private void checkForChanges() {
        if (originalProfileData == null) return;
        
        String firstName = Objects.requireNonNull(firstNameEditText.getText()).toString().trim();
        String lastName = Objects.requireNonNull(lastNameEditText.getText()).toString().trim();
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String phone = Objects.requireNonNull(phoneEditText.getText()).toString().trim();
        String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
        
        isDataModified = !firstName.equals(originalProfileData.get("firstName")) ||
                         !lastName.equals(originalProfileData.get("lastName")) ||
                         !email.equals(originalProfileData.get("email")) ||
                         (phone == null ? originalProfileData.get("phone") != null : !phone.equals(originalProfileData.get("phone"))) ||
                         !username.equals(originalProfileData.get("username")) ||
                         isPasswordModified;
        
        buttonsContainer.setVisibility(isDataModified ? View.VISIBLE : View.GONE);
    }
    
    private void loadUserProfile() {
        userApiService.getUserProfile("Bearer " + token).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    originalProfileData = response.body();
                    populateFields(originalProfileData);
                    
                    // Cargar la contraseña
                    if (originalProfileData.containsKey("userId")) {
                        String userId = (String) originalProfileData.get("userId");
                        loadUserPassword(userId);
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar el perfil: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"), 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadUserPassword(String userId) {
        userApiService.getUserPassword("Bearer " + token, userId).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, String>> call, @NonNull Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String password = response.body().get("password");
                    if (password != null) {
                        passwordEditText.setText(password);
                        isPasswordModified = false; // Marcamos como no modificado después de cargarlo
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, String>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error al cargar la contraseña: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void populateFields(Map<String, Object> profileData) {
        firstNameEditText.setText((String) profileData.get("firstName"));
        lastNameEditText.setText((String) profileData.get("lastName"));
        emailEditText.setText((String) profileData.get("email"));
        phoneEditText.setText((String) profileData.get("phone"));
        usernameEditText.setText((String) profileData.get("username"));
        
        // Mostrar el rol del usuario
        String role = (String) profileData.get("role");
        if (role != null) {
            String formattedRole;
            switch(role) {
                case "ADMIN":
                    formattedRole = "Administrador";
                    break;
                case "DEPARTMENT_HEAD":
                    formattedRole = "Jefe de Departamento";
                    break;
                            case "EMPLOYEE":
                formattedRole = "Empleado";
                    break;
                default:
                    formattedRole = role;
            }
            rolTextView.setText("Rol: " + formattedRole);
        } else {
            rolTextView.setText("Rol: --");
        }
        
        // La contraseña se carga por separado
        
        // Si es usuario admin, deshabilitar campos excepto contraseña
        boolean isAdmin = "ADMIN".equals(profileData.get("role"));
        if (isAdmin) {
            firstNameEditText.setEnabled(false);
            lastNameEditText.setEnabled(false);
            emailEditText.setEnabled(false);
            phoneEditText.setEnabled(false);
            usernameEditText.setEnabled(false);
        }
    }
    
    private void resetFields() {
        if (originalProfileData != null) {
            populateFields(originalProfileData);
            // Recargar también la contraseña
            if (originalProfileData.containsKey("userId")) {
                String userId = (String) originalProfileData.get("userId");
                loadUserPassword(userId);
            }
            buttonsContainer.setVisibility(View.GONE);
            isDataModified = false;
            isPasswordModified = false;
        }
    }
    
    private void updateProfile() {
        if (!isDataModified) return;
        
        // Si se ha modificado la contraseña, mostrar diálogo de confirmación
        if (isPasswordModified) {
            showPasswordConfirmationDialog();
        } else {
            // Si no hay cambio de contraseña, actualizar directamente
            performUpdate();
        }
    }
    
    private void showPasswordConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Cambio de contraseña")
                .setMessage("Has modificado tu contraseña. ¿Estás seguro de que deseas continuar?")
                .setPositiveButton("Aceptar", (dialog, which) -> performUpdate())
                .setNegativeButton("Cancelar", (dialog, which) -> resetFields())
                .show();
    }
    
    private void performUpdate() {
        Map<String, Object> updatedData = new HashMap<>();
        
        String firstName = Objects.requireNonNull(firstNameEditText.getText()).toString().trim();
        String lastName = Objects.requireNonNull(lastNameEditText.getText()).toString().trim();
        String email = Objects.requireNonNull(emailEditText.getText()).toString().trim();
        String phone = Objects.requireNonNull(phoneEditText.getText()).toString().trim();
        String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();
        
        updatedData.put("firstName", firstName);
        updatedData.put("lastName", lastName);
        updatedData.put("email", email);
        updatedData.put("phone", phone);
        updatedData.put("username", username);
        
        if (isPasswordModified) {
            updatedData.put("password", password);
        }
        
        userApiService.updateUserProfile("Bearer " + token, updatedData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    boolean passwordChanged = false;
                    if (response.body() != null) {
                        Object passwordChangedObj = response.body().get("passwordChanged");
                        if (passwordChangedObj instanceof Boolean) {
                            passwordChanged = (Boolean) passwordChangedObj;
                        }
                    }
                    
                    Toast.makeText(requireContext(), "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
                    
                    if (passwordChanged) {
                        Toast.makeText(requireContext(), "La contraseña ha sido actualizada", Toast.LENGTH_SHORT).show();
                    }
                    
                    // Recargar datos del perfil
                    loadUserProfile();
                    
                    // Ocultar botones
                    buttonsContainer.setVisibility(View.GONE);
                    isDataModified = false;
                    isPasswordModified = false;
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar el perfil: " + 
                            (response.errorBody() != null ? response.errorBody().toString() : "Desconocido"), 
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Restaurar toolbar al estado inicial sin botón de retroceso
        Toolbar toolbar = requireActivity().findViewById(R.id.materialToolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayHomeAsUpEnabled(false);
        Objects.requireNonNull(((AppCompatActivity) requireActivity()).getSupportActionBar()).setDisplayShowHomeEnabled(false);
    }
} 