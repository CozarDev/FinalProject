package com.proyectofinal.frontend.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Config.ApiConfig;
import com.proyectofinal.frontend.Adapters.MainPagerAdapter;
import com.proyectofinal.frontend.Fragments.CreateWorkReportFragment;
import com.proyectofinal.frontend.Fragments.HomeFragment;
import com.proyectofinal.frontend.Fragments.IncidencesFragment;
import com.proyectofinal.frontend.Fragments.ManageDepartmentsFragment;
import com.proyectofinal.frontend.Fragments.ManageEmployeesFragment;
import com.proyectofinal.frontend.Fragments.UserProfileFragment;
import com.proyectofinal.frontend.Fragments.ManageShiftTypesFragment;
import com.proyectofinal.frontend.Fragments.ManageShiftAssignmentsFragment;
import com.proyectofinal.frontend.Fragments.WorkReportsFragment;
import com.proyectofinal.frontend.R;

// 🔥 FIREBASE COMENTADO - DESCOMENTA SI QUIERES HABILITAR NOTIFICACIONES PUSH 🔥
// import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    MaterialToolbar materialToolbar;
    ViewPager2 viewPager2;
    BottomNavigationView bottomNavigationView;
    private ApiClient apiClient;
    private View fragmentContainer;
    
    private boolean isAdmin = false; 
    private boolean isDepartmentHead = false;
    private String currentRole = "";

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
        
        // Configurar ViewPager2 y BottomNavigationView
        setupViewPagerAndBottomNavigation();
        
        // Comprobar el rol del usuario
        checkUserRole();
        
        // 🔥 FIREBASE COMENTADO - DESCOMENTA SI QUIERES HABILITAR NOTIFICACIONES PUSH 🔥
        // initializeFirebaseMessaging();
    }
    
    private void checkUserRole() {
        // Verificar el rol del usuario basado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currentRole = prefs.getString("USER_ROLE", "EMPLOYEE"); // Asumir EMPLOYEE por defecto
        
        // Configurar flags de rol
        isAdmin = "ADMIN".equals(currentRole);
        isDepartmentHead = "DEPARTMENT_HEAD".equals(currentRole);
        
        Log.d("MainActivity", "Rol del usuario: " + currentRole);
        Log.d("MainActivity", "isAdmin: " + isAdmin + ", isDepartmentHead: " + isDepartmentHead);
        
        // Solo verificar si hay token, pero no mostrar Toast
        String token = prefs.getString("JWT_TOKEN", "");
        if (token.isEmpty()) {
            // Error silencioso, solo loguear
            Log.e("MainActivity", "Advertencia: No hay token JWT disponible");
        }
    }

    private void setupViewPagerAndBottomNavigation() {
        // Configurar el adaptador del ViewPager2
        MainPagerAdapter pagerAdapter = new MainPagerAdapter(this);
        viewPager2.setAdapter(pagerAdapter);
        
        // Configurar el menú del BottomNavigationView
        bottomNavigationView.inflateMenu(R.menu.bottom_navigation_menu);
        
        // Configurar listener para el BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                viewPager2.setCurrentItem(0, true);
                updateToolbarTitle("Inicio");
                // La notificación se hará automáticamente en onPageSelected del ViewPager2
                return true;
            } else if (itemId == R.id.nav_incidents) {
                viewPager2.setCurrentItem(1, true);
                updateToolbarTitle("Incidencias");
                // La notificación se hará automáticamente en onPageSelected del ViewPager2
                return true;
            } else if (itemId == R.id.nav_work_reports) {
                viewPager2.setCurrentItem(2, true);
                updateToolbarTitle("Partes de Trabajo");
                // La notificación se hará automáticamente en onPageSelected del ViewPager2
                return true;
            }
            return false;
        });
        
        // Configurar listener para sincronizar ViewPager2 con BottomNavigationView
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Log.d("MainActivity", "Página seleccionada: " + position);
                
                // Actualizar BottomNavigationView y título del toolbar
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.nav_home);
                        updateToolbarTitle("Inicio");
                        Log.d("MainActivity", "Navegando a Home - notificando selección");
                        notifyHomeFragmentSelected();
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.nav_incidents);
                        updateToolbarTitle("Incidencias");
                        Log.d("MainActivity", "Navegando a Incidences - notificando selección");
                        notifyIncidencesFragmentSelected();
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.nav_work_reports);
                        updateToolbarTitle("Partes de Trabajo");
                        Log.d("MainActivity", "Navegando a WorkReports - notificando selección");
                        notifyWorkReportsFragmentSelected();
                        break;
                }
            }
        });
        
        // Establecer la página inicial (Home)
        viewPager2.setCurrentItem(0, false);
        
        // Establecer título inicial
        updateToolbarTitle("Inicio");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.acciones_toolbar, menu);
        
        // Configurar visibilidad según rol
        MenuItem manageDepartments = menu.findItem(R.id.manageDepartments);
        MenuItem manageEmployees = menu.findItem(R.id.manageEmployees);
        MenuItem manageShifts = menu.findItem(R.id.manageShifts);
        MenuItem manageShiftAssignments = menu.findItem(R.id.manageShiftAssignments);
        
        // Configurar visibilidad para ADMIN
        if (manageDepartments != null) {
            manageDepartments.setVisible(isAdmin);
        }
        
        // Configurar visibilidad para ADMIN y DEPARTMENT_HEAD
        if (manageEmployees != null) {
            manageEmployees.setVisible(isAdmin || isDepartmentHead);
        }
        
        // Configurar visibilidad para ADMIN
        if (manageShifts != null) {
            manageShifts.setVisible(isAdmin);
        }
        
        // Configurar visibilidad para ADMIN y DEPARTMENT_HEAD
        if (manageShiftAssignments != null) {
            manageShiftAssignments.setVisible(isAdmin || isDepartmentHead);
        }
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.userProfile) {
            showUserProfileFragment();
            return true;
        } else if (id == R.id.manageDepartments) {
            showManageDepartmentsFragment();
            return true;
        } else if (id == R.id.manageEmployees) {
            showManageEmployeesFragment();
            return true;
        } else if (id == R.id.manageShifts) {
            showManageShiftsFragment();
            return true;
        } else if (id == R.id.manageShiftAssignments) {
            showManageShiftAssignmentsFragment();
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
        // Ocultar el ViewPager y mostrar el contenedor de fragmentos
        viewPager2.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Ocultar también el BottomNavigationView
        bottomNavigationView.setVisibility(View.GONE);
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Mi Perfil");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Crear una instancia del fragmento de perfil
        UserProfileFragment fragment = new UserProfileFragment();
        
        // Reemplazar el contenido actual con el fragmento
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null) // Permitir volver atrás con el botón atrás
                .commit();
    }

    private void showManageShiftsFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // Primero, mostrar el contenedor de fragmentos
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }
        
        // Ocultar el ViewPager si está visible
        if (viewPager2 != null) {
            viewPager2.setVisibility(View.GONE);
        }
        
        // Ocultar BottomNavigationView si está visible
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestionar Turnos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Crear y mostrar el fragmento
        ManageShiftTypesFragment manageShiftTypesFragment = new ManageShiftTypesFragment();
        transaction.replace(R.id.fragmentContainer, manageShiftTypesFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showManageShiftAssignmentsFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        
        // Primero, mostrar el contenedor de fragmentos
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }
        
        // Ocultar el ViewPager si está visible
        if (viewPager2 != null) {
            viewPager2.setVisibility(View.GONE);
        }
        
        // Ocultar BottomNavigationView si está visible
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Asignar Turnos");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Crear y mostrar el fragmento
        ManageShiftAssignmentsFragment manageShiftAssignmentsFragment = new ManageShiftAssignmentsFragment();
        transaction.replace(R.id.fragmentContainer, manageShiftAssignmentsFragment);
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
        
        // Mantener la página actual en lugar de volver siempre al inicio
        // if (viewPager2 != null) {
        //     viewPager2.setCurrentItem(0, true);
        // }
        
        // Restaurar estado del ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            
            // Restaurar el título según la página actual del ViewPager
            if (viewPager2 != null) {
                int currentPage = viewPager2.getCurrentItem();
                switch (currentPage) {
                    case 0:
                        updateToolbarTitle("Inicio");
                        break;
                    case 1:
                        updateToolbarTitle("Incidencias");
                        break;
                    case 2:
                        updateToolbarTitle("Partes de Trabajo");
                        break;
                    default:
                        updateToolbarTitle("Inicio");
                        break;
                }
            } else {
                updateToolbarTitle("Inicio");
            }
        }
        
        // Limpiar el listener de navegación del toolbar
        materialToolbar.setNavigationOnClickListener(null);
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

    public void showCreateWorkReportFragment() {
        // Ocultar el ViewPager y mostrar el contenedor de fragmentos
        viewPager2.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Ocultar también el BottomNavigationView
        bottomNavigationView.setVisibility(View.GONE);
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Nuevo Parte de Trabajo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Crear una instancia del fragmento
        CreateWorkReportFragment fragment = new CreateWorkReportFragment();
        
        // Configurar el listener para manejar eventos del fragmento
        fragment.setOnWorkReportCreatedListener(new CreateWorkReportFragment.OnWorkReportCreatedListener() {
            @Override
            public void onWorkReportCreated() {
                // Volver atrás y navegar a la pestaña de partes de trabajo
                onBackPressed();
                // Navegar a la pestaña de partes y notificar selección
                new Handler().postDelayed(() -> {
                    if (viewPager2 != null) {
                        viewPager2.setCurrentItem(2, true); // Índice 2 = WorkReports
                        Log.d("MainActivity", "Navegando a WorkReports después de crear parte");
                        
                        // Dar un poco más de tiempo y luego notificar la selección
                        new Handler().postDelayed(() -> {
                            notifyWorkReportsFragmentSelected();
                        }, 200);
                    }
                }, 300);
            }

            @Override
            public void onCancel() {
                onBackPressed(); // Volver atrás cuando se cancela
            }
        });
        
        // Reemplazar el contenido actual con el fragmento
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null) // Permitir volver atrás con el botón atrás
                .commit();
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
    
    // Método para reemplazar el fragmento actual con otro (usado para detalles de incidencias)
    public void replaceFragment(Fragment fragment) {
        Log.d("MainActivity", "Reemplazando fragmento: " + fragment.getClass().getSimpleName());
        
        // Ocultar ViewPager y BottomNavigation
        viewPager2.setVisibility(View.GONE);
        bottomNavigationView.setVisibility(View.GONE);
        
        // Mostrar el contenedor de fragmentos
        fragmentContainer.setVisibility(View.VISIBLE);
        
        // Configurar botón de retroceso en la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalles de Incidencia");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        materialToolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Reemplazar el fragmento
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
    
    // Método simplificado - el refresh automático se maneja en WorkReportListFragment
    private void refreshWorkReportsData() {
        // El WorkReportListFragment se refrescará automáticamente cuando se vuelva visible
        // No necesitamos lógica compleja aquí para evitar IllegalStateException
    }
    
    // Método simplificado para navegación
    private void refreshWorkReportsDataOnNavigation() {
        // El WorkReportListFragment se refrescará automáticamente cuando se vuelva visible
        // No necesitamos lógica compleja aquí para evitar IllegalStateException
    }
    
    // Método para notificar al WorkReportsFragment que fue seleccionado
    private void notifyWorkReportsFragmentSelected() {
        try {
            // Buscar el WorkReportsFragment usando el FragmentManager
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof WorkReportsFragment && fragment.isAdded()) {
                    ((WorkReportsFragment) fragment).onPageSelected();
                    
                    // También forzar refresh adicional con delay
                    new Handler().postDelayed(() -> {
                        if (fragment.isAdded() && !fragment.isDetached()) {
                            ((WorkReportsFragment) fragment).forceRefresh();
                        }
                    }, 300);
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error al notificar selección de WorkReportsFragment", e);
        }
    }

    // Método para notificar al HomeFragment que fue seleccionado
    private void notifyHomeFragmentSelected() {
        try {
            // Buscar el HomeFragment usando el FragmentManager
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof HomeFragment && fragment.isAdded()) {
                    ((HomeFragment) fragment).onPageSelected();
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error al notificar selección de HomeFragment", e);
        }
    }
    
    // Método para notificar al IncidencesFragment que fue seleccionado
    private void notifyIncidencesFragmentSelected() {
        try {
            // Buscar el IncidencesFragment usando el FragmentManager
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment instanceof IncidencesFragment && fragment.isAdded()) {
                    ((IncidencesFragment) fragment).onPageSelected();
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error al notificar selección de IncidencesFragment", e);
        }
    }
    
    // Método para actualizar el título del toolbar
    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
    
    // 🔥 FIREBASE COMENTADO - DESCOMENTA SI QUIERES HABILITAR NOTIFICACIONES PUSH 🔥
    // Método para inicializar Firebase Cloud Messaging
    /*
    private void initializeFirebaseMessaging() {
        try {
            // Verificar si Firebase está disponible
            Class.forName("com.google.firebase.messaging.FirebaseMessaging");
            
            // Solicitar permisos de notificación para Android 13+ (API 33+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != 
                    getPackageManager().PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
                }
            }
            
            // Inicializar Firebase Messaging
            com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w("MainActivity", "Error obteniendo token FCM", task.getException());
                            return;
                        }

                        // Obtener nuevo token FCM
                        String token = task.getResult();

                        // Guardar token localmente
                        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                        prefs.edit().putString("FCM_TOKEN", token).apply();

                        // Enviar token al servidor
                        sendTokenToServer(token);
                        
                        Log.i("MainActivity", "🔥 Firebase: HABILITADO - Notificaciones push configuradas");
                    });

            // Suscribirse a temas generales si es necesario
            com.google.firebase.messaging.FirebaseMessaging.getInstance().subscribeToTopic("general");
            
        } catch (ClassNotFoundException e) {
            // Firebase no está disponible
            Log.i("MainActivity", "🚫 Firebase: NO DISPONIBLE - Las notificaciones push están deshabilitadas");
            Log.i("MainActivity", "💡 Para habilitar notificaciones, configura google-services.json");
        } catch (Exception e) {
            // Error general de Firebase
            Log.w("MainActivity", "⚠️ Error inicializando Firebase: " + e.getMessage());
            Log.i("MainActivity", "📱 La app funcionará normalmente sin notificaciones push");
        }
    }
    */
     
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 🔥 FIREBASE COMENTADO - DESCOMENTA SI QUIERES HABILITAR NOTIFICACIONES PUSH 🔥
        /*
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED) {
                Toast.makeText(this, "Notificaciones habilitadas", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Las notificaciones están deshabilitadas. Puedes habilitarlas en Configuración.", Toast.LENGTH_LONG).show();
            }
        }
        */
    }
     
    // 🔥 FIREBASE COMENTADO - DESCOMENTA SI QUIERES HABILITAR NOTIFICACIONES PUSH 🔥
    // Método para enviar token FCM al servidor
    /*
    private void sendTokenToServer(String fcmToken) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userId = prefs.getString("USER_ID", "");
        String jwtToken = prefs.getString("JWT_TOKEN", "");
        
        if (!userId.isEmpty() && !jwtToken.isEmpty()) {
            // Crear request con información del dispositivo
            String deviceInfo = android.os.Build.MODEL + " (" + android.os.Build.VERSION.RELEASE + ")";
            
            // Usar el servicio FCM del ApiClient
            apiClient.getFCMApiService().registerFCMToken(
                "Bearer " + jwtToken, 
                new com.proyectofinal.frontend.Api.FCMApiService.FCMTokenRequest(userId, fcmToken, deviceInfo)
            ).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (!response.isSuccessful()) {
                        Log.e("MainActivity", "Error registrando token FCM: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Log.e("MainActivity", "Error de conexión registrando token FCM: " + t.getMessage());
                }
            });
        } else {
            Log.w("MainActivity", "No se puede enviar token FCM - usuario no autenticado");
        }
    }
    */
}
