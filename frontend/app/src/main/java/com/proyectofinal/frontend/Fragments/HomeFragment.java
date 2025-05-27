package com.proyectofinal.frontend.Fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.Models.ShiftAssignment;
import com.proyectofinal.frontend.Models.ShiftException;
import com.proyectofinal.frontend.Models.ShiftType;
import com.proyectofinal.frontend.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    
    private TextView welcomeTextView;
    private TextView nextShiftTextView;
    private CalendarView calendarView;
    private CardView dayInfoCard;
    private TextView dayInfoTextView;
    
    private ApiClient apiClient;
    private String userId;
    private String employeeId;
    private Employee currentEmployee;
    private List<ShiftAssignment> userShiftAssignments;
    private List<ShiftException> userShiftExceptions;
    private List<ShiftType> availableShiftTypes;
    
    // Variables para el seguimiento del calendario
    private Calendar currentVisibleMonth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Inicializar vistas
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        nextShiftTextView = view.findViewById(R.id.nextShiftTextView);
        calendarView = view.findViewById(R.id.calendarView);
        dayInfoCard = view.findViewById(R.id.dayInfoCard);
        dayInfoTextView = view.findViewById(R.id.dayInfoTextView);
        
        // Inicializar API client
        apiClient = ApiClient.getInstance(getContext());
        
        // Mostrar estado inicial
        welcomeTextView.setText("🔄 Inicializando aplicación...");
        nextShiftTextView.setText("⏳ Preparando conexión con el servidor...");
        
        // Obtener información del usuario
        getUserInfo();
        
        // Configurar calendario
        setupCalendar();
        
        // Ocultar inicialmente la tarjeta de información del día
        dayInfoCard.setVisibility(View.GONE);
    }
    
    private void getUserInfo() {
        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        userId = prefs.getString("USER_ID", "");
        String token = prefs.getString("JWT_TOKEN", "");
        String userRole = prefs.getString("USER_ROLE", "");
        
        if (userId.isEmpty()) {
            Log.e(TAG, "No se encontró ID de usuario en SharedPreferences");
            welcomeTextView.setText("Error: No se encontró información del usuario");
            return;
        }
        
        if (token.isEmpty()) {
            Log.e(TAG, "No se encontró token JWT en SharedPreferences");
            welcomeTextView.setText("Error: Sesión no válida, vuelve a iniciar sesión");
            return;
        }
        
        welcomeTextView.setText("Cargando información del usuario...");
        
        // Obtener información del empleado actual
        apiClient.getEmployeeApiService().getCurrentEmployeeInfo().enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(Call<Employee> call, Response<Employee> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentEmployee = response.body();
                    employeeId = currentEmployee.getId();
                    
                    // Configurar mensaje de bienvenida
                    setupWelcomeMessage();
                    
                    // Cargar datos del empleado
                    loadEmployeeData();
                } else {
                    Log.e(TAG, "Error al obtener empleado actual - Código: " + response.code());
                    if (response.code() == 401) {
                        welcomeTextView.setText("Sesión expirada, vuelve a iniciar sesión");
                    } else if (response.code() == 403) {
                        welcomeTextView.setText("Sin permisos para acceder a tu información de empleado");
                    } else {
                        welcomeTextView.setText("Error al cargar tu información de empleado (Código: " + response.code() + ")");
                    }
                }
            }
            
            @Override
            public void onFailure(Call<Employee> call, Throwable t) {
                Log.e(TAG, "Error de conexión al obtener empleado actual: ", t);
                welcomeTextView.setText("Error de conexión - Verifica tu internet y que el servidor esté funcionando");
            }
        });
    }
    
    private void setupWelcomeMessage() {
        if (currentEmployee != null) {
            String welcomeMessage = "¡Hola, " + currentEmployee.getFirstName() + "! ¿En qué puedo ayudarte en el día de hoy?";
            welcomeTextView.setText(welcomeMessage);
            nextShiftTextView.setText("Cargando información de turnos...");
        }
    }
    
    private void loadEmployeeData() {
        if (employeeId == null || employeeId.isEmpty()) {
            Log.e(TAG, "No se puede cargar datos del empleado - employeeId está vacío");
            nextShiftTextView.setText("Error: No se encontró ID del empleado");
            return;
        }
        
        nextShiftTextView.setText("Cargando asignaciones de turno...");
        
        // Cargar asignaciones de turno del empleado
        apiClient.getShiftAssignmentApiService().getShiftAssignmentsByEmployee(employeeId).enqueue(new Callback<List<ShiftAssignment>>() {
            @Override
            public void onResponse(Call<List<ShiftAssignment>> call, Response<List<ShiftAssignment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userShiftAssignments = response.body();
                    Log.d(TAG, "Asignaciones cargadas: " + userShiftAssignments.size());
                    
                    // Continuar con el siguiente paso
                    nextShiftTextView.setText("Cargando excepciones...");
                    loadShiftExceptions();
                } else {
                    Log.e(TAG, "Error al cargar asignaciones - Código: " + response.code());
                    nextShiftTextView.setText("Error al cargar asignaciones de turno (Código: " + response.code() + ")");
                }
            }
            
            @Override
            public void onFailure(Call<List<ShiftAssignment>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al cargar asignaciones: ", t);
                nextShiftTextView.setText("Error de conexión al cargar asignaciones: " + t.getMessage());
            }
        });
    }
    
    private void loadShiftExceptions() {
        apiClient.getHolidayApiService().getShiftExceptionsByEmployee(employeeId).enqueue(new Callback<List<ShiftException>>() {
            @Override
            public void onResponse(Call<List<ShiftException>> call, Response<List<ShiftException>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Filtrar solo festivos nacionales y vacaciones
                    userShiftExceptions = new ArrayList<>();
                    List<ShiftException> allExceptions = response.body();
                    
                    Log.d(TAG, "Total excepciones recibidas: " + allExceptions.size());
                    
                    for (ShiftException exception : allExceptions) {
                        Log.d(TAG, "Excepción: " + exception.getType() + " - Fecha: " + exception.getStartDate() + " - Desc: " + exception.getDescription());
                        
                        if ("NATIONAL_HOLIDAY".equals(exception.getType()) || 
                            "VACATION".equals(exception.getType())) {
                            userShiftExceptions.add(exception);
                        }
                    }
                    Log.d(TAG, "Excepciones filtradas (festivos y vacaciones): " + userShiftExceptions.size());
                    
                    // Continuar con el siguiente paso
                    nextShiftTextView.setText("Cargando tipos de turno...");
                    loadShiftTypes();
                } else {
                    Log.e(TAG, "Error al cargar excepciones - Código: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "Error leyendo error body: " + e.getMessage());
                        }
                    }
                    // No es crítico si fallan las excepciones, continuar
                    userShiftExceptions = new ArrayList<>();
                    nextShiftTextView.setText("Excepciones no disponibles, continuando...");
                    loadShiftTypes();
                }
            }
            
            @Override
            public void onFailure(Call<List<ShiftException>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al cargar excepciones: ", t);
                // No es crítico si fallan las excepciones, continuar
                userShiftExceptions = new ArrayList<>();
                nextShiftTextView.setText("Excepciones no disponibles, continuando...");
                loadShiftTypes();
            }
        });
    }
    
    private void loadShiftTypes() {
        apiClient.getShiftTypeApiService().getAllShiftTypes().enqueue(new Callback<List<ShiftType>>() {
            @Override
            public void onResponse(Call<List<ShiftType>> call, Response<List<ShiftType>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableShiftTypes = response.body();
                    Log.d(TAG, "Tipos de turno cargados: " + availableShiftTypes.size());
                    
                    // Ahora que tenemos todos los datos, calcular el próximo turno
                    nextShiftTextView.setText("Calculando próximo turno...");
                    calculateNextShift();
                    
                    // También actualizar el resumen del mes ahora que tenemos todos los datos
                    if (currentVisibleMonth != null) {
                        showMonthSummary(currentVisibleMonth.get(Calendar.YEAR), currentVisibleMonth.get(Calendar.MONTH));
                    }
                } else {
                    Log.e(TAG, "Error al cargar tipos de turno - Código: " + response.code());
                    nextShiftTextView.setText("Error al cargar tipos de turno (Código: " + response.code() + ")");
                }
            }
            
            @Override
            public void onFailure(Call<List<ShiftType>> call, Throwable t) {
                Log.e(TAG, "Error de conexión al cargar tipos de turno: ", t);
                nextShiftTextView.setText("Error de conexión al cargar tipos de turno: " + t.getMessage());
            }
        });
    }
    
    private void calculateNextShift() {
        if (userShiftAssignments == null || userShiftAssignments.isEmpty()) {
            nextShiftTextView.setText("No tienes turnos asignados");
            return;
        }
        
        if (availableShiftTypes == null || availableShiftTypes.isEmpty()) {
            nextShiftTextView.setText("No se pudieron cargar los tipos de turno");
            return;
        }
        
        Calendar today = Calendar.getInstance();
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        
        // Primero verificar si tenemos turno hoy y si ya ha empezado
        ShiftType todaysShift = getShiftForDate(todayStart.getTime());
        boolean todayHasException = isException(todayStart.getTime());
        
        if (todaysShift != null && !todayHasException) {
            // Hay turno hoy, verificar si ya ha empezado
            try {
                String[] startTimeParts = todaysShift.getStartTime().split(":");
                String[] endTimeParts = todaysShift.getEndTime().split(":");
                
                int startHour = Integer.parseInt(startTimeParts[0]);
                int startMinute = Integer.parseInt(startTimeParts[1]);
                int endHour = Integer.parseInt(endTimeParts[0]);
                int endMinute = Integer.parseInt(endTimeParts[1]);
                
                Calendar currentTime = Calendar.getInstance();
                int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
                int currentMinute = currentTime.get(Calendar.MINUTE);
                
                // Convertir todo a minutos para comparar fácilmente
                int startTimeInMinutes = startHour * 60 + startMinute;
                int endTimeInMinutes = endHour * 60 + endMinute;
                int currentTimeInMinutes = currentHour * 60 + currentMinute;
                
                // Manejar turnos que cruzan la medianoche
                if (endTimeInMinutes < startTimeInMinutes) {
                    // Turno nocturno que cruza medianoche
                    if (currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes <= endTimeInMinutes) {
                        nextShiftTextView.setText("🟢 Tu jornada laboral actual está en curso (termina a las " + todaysShift.getEndTime() + ")");
                        return;
                    }
                } else {
                    // Turno normal dentro del mismo día
                    if (currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes <= endTimeInMinutes) {
                        nextShiftTextView.setText("🟢 Tu jornada laboral actual está en curso (termina a las " + todaysShift.getEndTime() + ")");
                        return;
                    }
                }
                
                // Si llegamos aquí, el turno de hoy no ha empezado aún
                if (currentTimeInMinutes < startTimeInMinutes) {
                    nextShiftTextView.setText("⏰ Tu próximo turno es hoy a las " + todaysShift.getStartTime());
                    return;
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error procesando horarios del turno de hoy: " + e.getMessage());
                // Continuar con la lógica normal si hay error
            }
        }
        
        // Buscar el próximo turno en los próximos 30 días (excluyendo hoy si ya pasó)
        for (int i = 1; i < 30; i++) { // Empezar desde 1 (mañana)
            Calendar checkDate = (Calendar) todayStart.clone();
            checkDate.add(Calendar.DAY_OF_MONTH, i);
            
            ShiftType shiftForDay = getShiftForDate(checkDate.getTime());
            boolean hasException = isException(checkDate.getTime());
            
            if (shiftForDay != null && !hasException) {
                String dayText;
                if (i == 1) {
                    dayText = "mañana";
                } else if (i == 2) {
                    dayText = "pasado mañana";
                } else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM", Locale.getDefault());
                    dayText = "el " + dateFormat.format(checkDate.getTime());
                }
                
                String nextShiftMessage = "📅 Tu próximo turno es " + dayText + " a las " + shiftForDay.getStartTime();
                nextShiftTextView.setText(nextShiftMessage);
                return;
            }
        }
        
        nextShiftTextView.setText("📅 No tienes turnos programados en los próximos 30 días");
    }
    
    private void setupCalendar() {
        // Configurar el calendario para mostrar solo el año actual
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        long minDate = calendar.getTimeInMillis();
        
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        long maxDate = calendar.getTimeInMillis();
        
        calendarView.setMinDate(minDate);
        calendarView.setMaxDate(maxDate);
        
        // Inicializar el mes visible actual
        currentVisibleMonth = Calendar.getInstance();
        
        // Configurar listener para cuando se selecciona una fecha
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            
            Log.d(TAG, "Fecha seleccionada: " + year + "-" + (month + 1) + "-" + dayOfMonth);
            
            // Actualizar el mes visible si es necesario
            if (currentVisibleMonth.get(Calendar.YEAR) != year || 
                currentVisibleMonth.get(Calendar.MONTH) != month) {
                
                currentVisibleMonth.set(year, month, 1);
                Log.d(TAG, "Mes visible cambiado a: " + year + "-" + (month + 1));
                
                // Aquí podríamos agregar lógica adicional cuando cambia el mes
                showMonthSummary(year, month);
            }
            
            showDayInfo(selectedDate.getTime());
        });
        
        // Mostrar resumen del mes actual
        showMonthSummary(currentVisibleMonth.get(Calendar.YEAR), currentVisibleMonth.get(Calendar.MONTH));
    }
    
    private void showMonthSummary(int year, int month) {
        if (userShiftAssignments == null || userShiftExceptions == null || availableShiftTypes == null) {
            Log.d(TAG, "Datos aún no cargados, saltando resumen del mes");
            return;
        }
        
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(year, month, 1, 0, 0, 0);
        monthStart.set(Calendar.MILLISECOND, 0);
        
        Calendar monthEnd = Calendar.getInstance();
        monthEnd.set(year, month, monthStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        monthEnd.set(Calendar.MILLISECOND, 999);
        
        int workDays = 0;
        int vacationDays = 0;
        int holidayDays = 0;
        int restDays = 0;
        
        // Iterar por todos los días del mes
        Calendar day = (Calendar) monthStart.clone();
        int daysInMonth = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int dayNum = 1; dayNum <= daysInMonth; dayNum++) {
            day.set(Calendar.DAY_OF_MONTH, dayNum);
            Date dayDate = day.getTime();
            
            if (isException(dayDate)) {
                ShiftException exception = getExceptionForDate(dayDate);
                if (exception != null) {
                    switch (exception.getType()) {
                        case "VACATION":
                            vacationDays++;
                            break;
                        case "NATIONAL_HOLIDAY":
                            holidayDays++;
                            break;
                    }
                }
            } else if (getShiftForDate(dayDate) != null) {
                workDays++;
            } else {
                restDays++;
            }
        }
        
        String monthName = new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthStart.getTime());
        Log.d(TAG, String.format("Resumen de %s - Laborales: %d, Vacaciones: %d, Festivos: %d, Descanso: %d", 
                monthName, workDays, vacationDays, holidayDays, restDays));
        
        // Aquí podrías mostrar este resumen en la UI si quisieras
        // Por ejemplo, en un Toast o en una vista adicional
        if (getContext() != null && (vacationDays > 0 || holidayDays > 0)) {
            String summaryMessage = "📅 " + monthName + ": ";
            if (vacationDays > 0) summaryMessage += vacationDays + " días de vacaciones ";
            if (holidayDays > 0) summaryMessage += holidayDays + " festivos ";
            
            Toast.makeText(getContext(), summaryMessage, Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showDayInfo(Date selectedDate) {
        String dayInfo = getDayInfo(selectedDate);
        dayInfoTextView.setText(dayInfo);
        
        // Mostrar la tarjeta con animación
        if (dayInfoCard.getVisibility() == View.GONE) {
            dayInfoCard.setVisibility(View.VISIBLE);
            
            // Animación de aparición
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(dayInfoCard, "scaleX", 0f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(dayInfoCard, "scaleY", 0f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(dayInfoCard, "alpha", 0f, 1f);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(300);
            animatorSet.start();
        }
        
        // Configurar listener para ocultar la tarjeta al tocar en ella
        dayInfoCard.setOnClickListener(v -> hideDayInfo());
    }
    
    private void hideDayInfo() {
        if (dayInfoCard.getVisibility() == View.VISIBLE) {
            // Animación de desaparición
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(dayInfoCard, "scaleX", 1f, 0f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(dayInfoCard, "scaleY", 1f, 0f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(dayInfoCard, "alpha", 1f, 0f);
            
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(scaleX, scaleY, alpha);
            animatorSet.setDuration(200);
            animatorSet.start();
            
            animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    dayInfoCard.setVisibility(View.GONE);
                }
            });
        }
    }
    
    private String getDayInfo(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        
        Log.d(TAG, "Obteniendo información para fecha: " + formattedDate);
        
        // Verificar si es una excepción (solo festivos y vacaciones)
        boolean isExceptionDay = isException(date);
        Log.d(TAG, "¿Es día de excepción?: " + isExceptionDay);
        
        if (isExceptionDay) {
            ShiftException exception = getExceptionForDate(date);
            if (exception != null) {
                Log.d(TAG, "Excepción encontrada: " + exception.getType() + " - " + exception.getDescription());
                switch (exception.getType()) {
                    case "NATIONAL_HOLIDAY":
                        return "📅 " + formattedDate + "\n\n🎉 Día festivo nacional\n" + 
                               (exception.getDescription() != null && !exception.getDescription().isEmpty() ? 
                                exception.getDescription() : "Festivo nacional") + "\n\n💡 Toca aquí para cerrar";
                    case "VACATION":
                        return "📅 " + formattedDate + "\n\n🏖️ Día de vacaciones\n" + 
                               "Disfruta tu día libre\n\n💡 Toca aquí para cerrar";
                    default:
                        Log.w(TAG, "Tipo de excepción no manejado: " + exception.getType());
                        break;
                }
            } else {
                Log.w(TAG, "isException devolvió true pero getExceptionForDate devolvió null");
            }
        }
        
        // Verificar si tiene turno asignado
        ShiftType shiftForDay = getShiftForDate(date);
        Log.d(TAG, "Turno para el día: " + (shiftForDay != null ? shiftForDay.getName() : "null"));
        
        if (shiftForDay != null) {
            return "📅 " + formattedDate + "\n\n💼 Día laboral\n" + 
                   "Turno: " + shiftForDay.getName() + "\n" +
                   "Horario: " + shiftForDay.getStartTime() + " - " + shiftForDay.getEndTime() + 
                   "\n\n💡 Toca aquí para cerrar";
        }
        
        // Día de descanso
        Log.d(TAG, "Día de descanso - sin turno ni excepción");
        return "📅 " + formattedDate + "\n\n😴 Día de descanso\n" + 
               "No tienes turno asignado\n\n💡 Toca aquí para cerrar";
    }
    
    private ShiftType getShiftForDate(Date date) {
        if (userShiftAssignments == null || availableShiftTypes == null) {
            return null;
        }
        
        // Buscar asignación activa para esta fecha
        for (ShiftAssignment assignment : userShiftAssignments) {
            if (assignment.isActiveOn(date)) {
                // Buscar el tipo de turno correspondiente
                for (ShiftType shiftType : availableShiftTypes) {
                    if (shiftType.getId().equals(assignment.getShiftTypeId())) {
                        // Verificar si el día de la semana está incluido en el turno
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        
                        if (isDayIncludedInShift(shiftType, dayOfWeek)) {
                            return shiftType;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    private boolean isDayIncludedInShift(ShiftType shiftType, int dayOfWeek) {
        if (shiftType.getWorkDays() == null || shiftType.getWorkDays().length != 7) {
            return false;
        }
        
        boolean[] workDays = shiftType.getWorkDays();
        
        int arrayIndex;
        String dayName;
        switch (dayOfWeek) {
            case Calendar.MONDAY:
                arrayIndex = 0;
                dayName = "Lunes";
                break;
            case Calendar.TUESDAY:
                arrayIndex = 1;
                dayName = "Martes";
                break;
            case Calendar.WEDNESDAY:
                arrayIndex = 2;
                dayName = "Miércoles";
                break;
            case Calendar.THURSDAY:
                arrayIndex = 3;
                dayName = "Jueves";
                break;
            case Calendar.FRIDAY:
                arrayIndex = 4;
                dayName = "Viernes";
                break;
            case Calendar.SATURDAY:
                arrayIndex = 5;
                dayName = "Sábado";
                break;
            case Calendar.SUNDAY:
                arrayIndex = 6;
                dayName = "Domingo";
                break;
            default:
                return false;
        }
        
        boolean isIncluded = workDays[arrayIndex];
        
        return isIncluded;
    }
    
    private boolean isException(Date date) {
        if (userShiftExceptions == null) {
            Log.d(TAG, "userShiftExceptions es null");
            return false;
        }

        if (userShiftExceptions.isEmpty()) {
            Log.d(TAG, "userShiftExceptions está vacío");
            return false;
        }

        SimpleDateFormat debugFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String targetDateStr = debugFormat.format(date);
        
        Log.d(TAG, "Verificando excepciones para fecha: " + targetDateStr + " (total excepciones: " + userShiftExceptions.size() + ")");
        
        for (ShiftException exception : userShiftExceptions) {
            if (exception.getStartDate() != null) {
                // Para excepciones de un solo día (como festivos nacionales)
                if (exception.getEndDate() == null || isSameDay(exception.getStartDate(), exception.getEndDate())) {
                    String exceptionDateStr = debugFormat.format(exception.getStartDate());
                    boolean isSame = isSameDay(exception.getStartDate(), date);
                    
                    Log.d(TAG, "Comparando con excepción " + exception.getType() + " fecha: " + exceptionDateStr + " - ¿Es igual?: " + isSame);
                    
                    if (isSame) {
                        Log.d(TAG, "¡Encontrada excepción coincidente!");
                        return true;
                    }
                } else {
                    // Para excepciones de rango (como vacaciones)
                    String startDateStr = debugFormat.format(exception.getStartDate());
                    String endDateStr = debugFormat.format(exception.getEndDate());
                    boolean isInRange = isDateInRange(date, exception.getStartDate(), exception.getEndDate());
                    
                    Log.d(TAG, "Comparando con excepción " + exception.getType() + " rango: " + startDateStr + " a " + endDateStr + " - ¿Está en rango?: " + isInRange);
                    
                    if (isInRange) {
                        Log.d(TAG, "¡Encontrada excepción de rango coincidente!");
                        return true;
                    }
                }
            } else {
                Log.w(TAG, "Excepción con startDate null: " + exception.getType());
            }
        }
        
        Log.d(TAG, "No se encontraron excepciones para la fecha");
        return false;
    }
    
    private ShiftException getExceptionForDate(Date date) {
        if (userShiftExceptions == null) {
            return null;
        }
        
        for (ShiftException exception : userShiftExceptions) {
            if (exception.getStartDate() != null) {
                // Para excepciones de un solo día
                if (exception.getEndDate() == null || isSameDay(exception.getStartDate(), exception.getEndDate())) {
                    if (isSameDay(exception.getStartDate(), date)) {
                        return exception;
                    }
                } else {
                    // Para excepciones de rango
                    if (isDateInRange(date, exception.getStartDate(), exception.getEndDate())) {
                        return exception;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Verifica si una fecha está dentro de un rango (inclusive)
     */
    private boolean isDateInRange(Date targetDate, Date startDate, Date endDate) {
        if (targetDate == null || startDate == null || endDate == null) {
            Log.w(TAG, "isDateInRange: Una de las fechas es null");
            return false;
        }
        
        // Normalizar las fechas a medianoche para comparación
        Calendar target = Calendar.getInstance();
        target.setTime(targetDate);
        target.set(Calendar.HOUR_OF_DAY, 0);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);
        
        Calendar start = Calendar.getInstance();
        start.setTime(startDate);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        
        Calendar end = Calendar.getInstance();
        end.setTime(endDate);
        end.set(Calendar.HOUR_OF_DAY, 23);
        end.set(Calendar.MINUTE, 59);
        end.set(Calendar.SECOND, 59);
        end.set(Calendar.MILLISECOND, 999);
        
        long targetTime = target.getTimeInMillis();
        long startTime = start.getTimeInMillis();
        long endTime = end.getTimeInMillis();
        
        boolean inRange = targetTime >= startTime && targetTime <= endTime;
        
        SimpleDateFormat debugFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Log.d(TAG, "Verificando rango:");
        Log.d(TAG, "  Fecha objetivo: " + debugFormat.format(targetDate));
        Log.d(TAG, "  Inicio: " + debugFormat.format(startDate));
        Log.d(TAG, "  Fin: " + debugFormat.format(endDate));
        Log.d(TAG, "  ¿Está en rango?: " + inRange);
        
        return inRange;
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            Log.w(TAG, "isSameDay: Una de las fechas es null - date1: " + (date1 != null) + ", date2: " + (date2 != null));
            return false;
        }
        
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        
        boolean sameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        
        // Logs de debug
        SimpleDateFormat debugFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d(TAG, "Comparando fechas:");
        Log.d(TAG, "  Fecha 1: " + debugFormat.format(date1) + " (año: " + cal1.get(Calendar.YEAR) + ", día del año: " + cal1.get(Calendar.DAY_OF_YEAR) + ")");
        Log.d(TAG, "  Fecha 2: " + debugFormat.format(date2) + " (año: " + cal2.get(Calendar.YEAR) + ", día del año: " + cal2.get(Calendar.DAY_OF_YEAR) + ")");
        Log.d(TAG, "  ¿Mismo año?: " + sameYear + ", ¿Mismo día?: " + sameDay + ", Resultado: " + (sameYear && sameDay));
        
        return sameYear && sameDay;
    }

    // Método para ser llamado cuando se selecciona esta pestaña
    public void onPageSelected() {
        Log.d(TAG, "onPageSelected() - HomeFragment seleccionado");
        
        // Recargar datos del usuario y turnos
        if (getView() != null && isAdded() && !isDetached()) {
            Log.d(TAG, "Refrescando datos del HomeFragment");
            
            // Mostrar mensaje de actualización
            if (welcomeTextView != null) {
                welcomeTextView.setText("🔄 Actualizando información...");
            }
            if (nextShiftTextView != null) {
                nextShiftTextView.setText("⏳ Cargando datos actualizados...");
            }
            
            // Recargar información del usuario
            getUserInfo();
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() - HomeFragment visible");
        
        // Actualizar datos cuando el fragmento vuelve a ser visible
        if (getView() != null && isAdded()) {
            Log.d(TAG, "Refrescando datos en onResume");
            getUserInfo();
        }
    }
} 