package com.proyectofinal.frontend.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Fragments.ManageDepartmentsFragment;
import com.proyectofinal.frontend.Fragments.ManageEmployeesFragment;
import com.proyectofinal.frontend.Fragments.UserProfileFragment;
import com.proyectofinal.frontend.Models.Department;
import com.proyectofinal.frontend.R;
import androidx.appcompat.widget.ActionMenuView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    MaterialToolbar materialToolbar;
    ViewPager2 viewPager2;
    BottomNavigationView bottomNavigationView;
    private ApiClient apiClient;
    private View fragmentContainer;
    
    // Claves para SharedPreferences
    private static final String PREF_TOOLBAR_GUIDE_SHOWN = "toolbar_guide_shown";
    private static final String PREF_DEPARTMENT_MENU_GUIDE_SHOWN = "department_menu_guide_shown";
    private boolean shouldShowGuides = false;
    private boolean isAdmin = false; // Aquí deberías agregar lógica para detectar si es admin

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        materialToolbar = findViewById(R.id.materialToolbar);
        viewPager2 = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fragmentContainer = findViewById(R.id.fragmentContainer);
        setSupportActionBar(materialToolbar);

        // Inicializar API client
        apiClient = ApiClient.getInstance(this);
        
        // Comprobar si el usuario es administrador
        checkIfUserIsAdmin();
        
        // Verificar si hay departamentos (solo para administradores)
        if (isAdmin) {
            checkIfDepartmentsExist();
        }
    }
    
    private void checkIfUserIsAdmin() {
        // Verificar el rol del usuario basado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String role = prefs.getString("USER_ROLE", "ADMIN"); // Asumir ADMIN por defecto
        isAdmin = "ADMIN".equals(role);
        
        // Solo verificar si hay token, pero no mostrar Toast
        String token = prefs.getString("JWT_TOKEN", "");
        if (token.isEmpty()) {
            // Error silencioso, solo loguear
            System.out.println("Advertencia: No hay token JWT disponible");
        }
    }
    
    private void checkIfDepartmentsExist() {
        // Si ya se mostraron todas las guías, no hacer nada más.
        if (isToolbarGuideShown() && isDepartmentMenuGuideShown()) {
            return;
        }

        // Crear un Retrofit para hacer la consulta directamente
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/") 
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        // Crear la interfaz para la llamada
        DepartmentApiService apiService = retrofit.create(DepartmentApiService.class);
        
        // Realizar la llamada para verificar si hay departamentos
        apiService.getAllDepartments().enqueue(new Callback<List<Department>>() {
            @Override
            public void onResponse(Call<List<Department>> call, Response<List<Department>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Department> departments = response.body();
                    // Si no hay departamentos o la lista está vacía
                    if (departments == null || departments.isEmpty()) {
                        // No hay departamentos, activar la bandera para mostrar la guía secuencial
                        // La guía se mostrará en onCreateOptionsMenu para asegurar que el menú esté listo
                        shouldShowGuides = true;
                        // Forzar que el menú se invalide y se vuelva a crear para llamar a onCreateOptionsMenu
                        invalidateOptionsMenu(); 
                    }
                } else {
                    // Error en la respuesta, probablemente no exista la colección
                    shouldShowGuides = true;
                    invalidateOptionsMenu();
                }
            }

            @Override
            public void onFailure(Call<List<Department>> call, Throwable t) {
                // Error de conexión, asumimos que no hay departamentos
                shouldShowGuides = true;
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.acciones_toolbar, menu);
        
        // Si debemos mostrar guías y la guía del toolbar aún no se ha mostrado,
        // iniciarla con un retraso.
        if (shouldShowGuides && !isToolbarGuideShown()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Verificar de nuevo antes de mostrar, por si acaso
                if (shouldShowGuides && !isToolbarGuideShown()) {
                    showToolbarMenuGuide();
                    // Una vez que se inicia la secuencia de guías, desactivar la bandera.
                    shouldShowGuides = false; 
                }
            }, 300); // Retraso para asegurar que el menú está listo.
        }
        
        return super.onCreateOptionsMenu(menu);
    }
    
    private void showToolbarMenuGuide() {
        // Verificar si ya se mostró la guía anteriormente
        if (isToolbarGuideShown()) {
            showDepartmentMenuGuide();
            return;
        }
        
        // Obtener el botón de overflow del toolbar
        View overflowButton = null;
        // Intentar obtener el botón overflow de manera más precisa
        try {
            // Buscar el último hijo visible del toolbar, que suele ser el menú overflow
            if (materialToolbar != null) {
                for (int i = 0; i < materialToolbar.getChildCount(); i++) {
                    View child = materialToolbar.getChildAt(i);
                    if (child instanceof ActionMenuView) {
                        ActionMenuView actionMenuView = (ActionMenuView) child;
                        // El último elemento suele ser el botón de overflow
                        if (actionMenuView.getChildCount() > 0) {
                            overflowButton = actionMenuView.getChildAt(actionMenuView.getChildCount() - 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Si hay algún error, seguimos con el enfoque alternativo
            e.printStackTrace();
        }
        
        // Si no pudimos encontrar el botón de manera precisa, usar la alternativa
        if (overflowButton == null) {
            // Alternativa: Apuntar al toolbar completo como fallback
            overflowButton = materialToolbar;
        }
        
        // Usar TapTargetView para mostrar la guía sobre el botón de menú
        final View targetView = overflowButton;
        TapTargetView.showFor(this,
            TapTarget.forView(targetView, "Menú de opciones", 
                         "Pulsa en los tres puntos para acceder a las opciones de administración")
                .outerCircleColor(R.color.colorPrimary)
                .targetCircleColor(R.color.white)
                .textColor(R.color.white)
                .textTypeface(Typeface.SANS_SERIF)
                .tintTarget(true)
                .drawShadow(true)
                .cancelable(true)
                .targetRadius(60),
            new TapTargetView.Listener() {
                @Override
                public void onTargetClick(TapTargetView view) {
                    super.onTargetClick(view);
                    // Guardar que ya se mostró la guía
                    saveToolbarGuideShown();
                    // Mostrar la siguiente guía
                    showDepartmentMenuGuide();
                }
                
                @Override
                public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                    if (userInitiated) {
                        // El usuario descartó la guía
                        saveToolbarGuideShown();
                        // Mostrar la siguiente guía
                        showDepartmentMenuGuide();
                    }
                }
            }
        );
    }
    
    private void showDepartmentMenuGuide() {
        // Verificar si ya se mostró la guía anteriormente
        if (isDepartmentMenuGuideShown()) {
            return;
        }
        
        // Guardar que ya se mostró esta guía
        saveDepartmentMenuGuideShown();
        
        // Intentar abrir el menú de opciones programáticamente
        openOptionsMenu();
        
        // Después de abrirlo, intentar encontrar y señalar la opción "Gestionar Departamentos"
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Buscar el ítem directamente
            findAndShowMenuItemTarget();
        }, 500); // Tiempo suficiente para que se despliegue el menú
    }
    
    // Método para encontrar y mostrar la guía sobre el ítem del menú
    private void findAndShowMenuItemTarget() {
        // Buscar recursivamente todas las vistas para encontrar el TextView con el texto "Gestionar Departamentos"
        View rootView = getWindow().getDecorView();
        findMenuItemAndShowGuide(rootView);
    }
    
    // Método recursivo para buscar el ítem del menú
    private void findMenuItemAndShowGuide(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                
                // Depuración de vistas encontradas (si es TextView)
                if (child instanceof TextView) {
                    TextView textView = (TextView) child;
                    String text = textView.getText().toString();
                    
                    // Si encontramos el ítem "Gestionar Departamentos"
                    if ("Gestionar Departamentos".equals(text)) {
                        // Usar TapTargetView para mostrar la guía sobre el item del menú
                        TapTargetView.showFor(this,
                            TapTarget.forView(textView, "Gestión de departamentos", 
                                         "Pulsa aquí para crear y administrar los departamentos de la empresa")
                                .outerCircleColor(R.color.colorPrimary)
                                .targetCircleColor(R.color.white)
                                .textColor(R.color.white)
                                .textTypeface(Typeface.SANS_SERIF)
                                .tintTarget(true)
                                .drawShadow(true)
                                .cancelable(true));
                        return; // Terminar la búsqueda
                    }
                }
                
                // Seguir buscando recursivamente
                findMenuItemAndShowGuide(child);
            }
        }
    }
    
    private boolean isToolbarGuideShown() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean(PREF_TOOLBAR_GUIDE_SHOWN, false);
    }
    
    private void saveToolbarGuideShown() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_TOOLBAR_GUIDE_SHOWN, true)
                .apply();
    }
    
    private boolean isDepartmentMenuGuideShown() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE).getBoolean(PREF_DEPARTMENT_MENU_GUIDE_SHOWN, false);
    }
    
    private void saveDepartmentMenuGuideShown() {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean(PREF_DEPARTMENT_MENU_GUIDE_SHOWN, true)
                .apply();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.manageDepartments) {
            showManageDepartmentsFragment();
            return true;
        } else if (id == R.id.manageEmployees) {
            showManageEmployeesFragment();
            return true;
        } else if (id == R.id.userProfile) {
            showUserProfileFragment();
            return true;
        } else if (id == R.id.logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showManageDepartmentsFragment() {
        // Ocultar el ViewPager y mostrar el contenedor de fragmentos
        viewPager2.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Ocultar también el BottomNavigationView
        bottomNavigationView.setVisibility(View.GONE);
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestionar Departamentos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Crear una instancia del fragmento
        ManageDepartmentsFragment fragment = new ManageDepartmentsFragment();
        
        // Reemplazar el contenido actual con el fragmento
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null) // Permitir volver atrás con el botón atrás
                .commit();
    }
    
    private void showManageEmployeesFragment() {
        // Ocultar el ViewPager y mostrar el contenedor de fragmentos
        viewPager2.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Ocultar también el BottomNavigationView
        bottomNavigationView.setVisibility(View.GONE);
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestionar Empleados");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Crear una instancia del fragmento
        ManageEmployeesFragment fragment = new ManageEmployeesFragment();
        
        // Reemplazar el contenido actual con el fragmento
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null) // Permitir volver atrás con el botón atrás
                .commit();
    }

    private void showUserProfileFragment() {
        // Ocultar el ViewPager y bottomNavigationView
        if (viewPager2 != null) viewPager2.setVisibility(View.GONE);
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
        
        // Mostrar el contenedor de fragmentos
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi Perfil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Crear y mostrar el fragmento de perfil de usuario
        UserProfileFragment userProfileFragment = new UserProfileFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, userProfileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        // Si hay fragmentos en el backstack, manejar normalmente
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            
            // Verificar si quedan fragmentos después de popBackStack
            // Si no quedan, restaurar la vista principal
            Handler handler = new Handler();
            handler.post(() -> {
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    restoreMainView();
                }
            });
        } else {
            super.onBackPressed();
        }
    }

    private void restoreMainView() {
        // Restaurar visibilidad del ViewPager y bottomNavigationView
        if (viewPager2 != null) viewPager2.setVisibility(View.VISIBLE);
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);
        
        // Ocultar el contenedor de fragmentos
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        
        // Restaurar estado del ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = prefs.getString("JWT_TOKEN", "");

        if (token.isEmpty()) {
            // Si no hay token, simplemente volvemos a la pantalla de login
            clearSessionAndRedirect();
            return;
        }

        // Llamar al endpoint de logout
        apiClient.getApiService().logout("Bearer " + token).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                // Independientemente de la respuesta, limpiamos la sesión
                clearSessionAndRedirect();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                // En caso de error, informamos al usuario pero igualmente cerramos sesión localmente
                Toast.makeText(MainActivity.this, 
                        "Error al comunicarse con el servidor: " + t.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                clearSessionAndRedirect();
            }
        });
    }

    private void clearSessionAndRedirect() {
        // Limpiar datos de sesión
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Redirigir a la pantalla de login
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // Limpiar la pila de actividades para que el usuario no pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    // Interfaz para la API de Departamentos (sólo para esta actividad)
    private interface DepartmentApiService {
        @retrofit2.http.GET("departments")
        Call<List<Department>> getAllDepartments();
    }
}