package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.ShiftException;
import com.proyectofinal.backend.Repositories.ShiftExceptionRepository;
// 🔥 CALENDARIFIC COMENTADO - DESCOMENTA SI QUIERES HABILITAR IMPORTACIÓN DE FESTIVOS 🔥
// import com.proyectofinal.backend.Services.CalendarificService;
import com.proyectofinal.backend.Services.ScheduledTasksService;
import com.proyectofinal.backend.Services.UserService;
import com.proyectofinal.backend.Services.VacationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    // 🔥 CALENDARIFIC COMENTADO - DESCOMENTA SI QUIERES HABILITAR IMPORTACIÓN DE FESTIVOS 🔥
    // private final CalendarificService calendarificService;
    private final VacationService vacationService;
    private final ShiftExceptionRepository shiftExceptionRepository;
    private final UserService userService;
    private final ScheduledTasksService scheduledTasksService;
    private static final Logger logger = LoggerFactory.getLogger(HolidayController.class);

    public HolidayController(
            // CalendarificService calendarificService,  // 🔥 COMENTADO - Calendarific deshabilitado
            VacationService vacationService,
            ShiftExceptionRepository shiftExceptionRepository,
            UserService userService,
            ScheduledTasksService scheduledTasksService) {
        // this.calendarificService = calendarificService;  // 🔥 COMENTADO - Calendarific deshabilitado
        this.vacationService = vacationService;
        this.shiftExceptionRepository = shiftExceptionRepository;
        this.userService = userService;
        this.scheduledTasksService = scheduledTasksService;
        
        logger.info("🚫 HolidayController: Calendarific deshabilitado - Importación automática de festivos no disponible");
        logger.info("💡 Los festivos pueden añadirse manualmente desde la interfaz de administración");
    }

    /**
     * Importa festivos nacionales para un año específico
     * 🔥 FUNCIONALIDAD DESHABILITADA - Requiere Calendarific API
     */
    @PostMapping("/import-national")
    public ResponseEntity<?> importNationalHolidays(@RequestParam(defaultValue = "0") int year) {
        // Verificar permisos (solo admin puede importar festivos)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden importar festivos");
        }
        
        // 🔥 FUNCIONALIDAD DESHABILITADA
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Importación automática de festivos deshabilitada");
        response.put("reason", "Calendarific API no está configurada");
        response.put("solution", "Los festivos pueden añadirse manualmente desde la interfaz de administración");
        response.put("year", year);
        
        logger.warn("🚫 Intento de importación de festivos para año {} - Funcionalidad deshabilitada", year);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        
        /* 🔥 CÓDIGO COMENTADO - DESCOMENTA PARA HABILITAR CALENDARIFIC
        // Si no se especifica un año, usar el año actual
        if (year <= 0) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        
        // Verificar si ya existen festivos para ese año
        if (calendarificService.holidaysExistForYear(year)) {
            return ResponseEntity.badRequest()
                    .body("Ya existen festivos importados para el año " + year);
        }
        
        // Importar festivos
        int count = calendarificService.importNationalHolidays(year);
        
        if (count > 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Se importaron " + count + " festivos nacionales para el año " + year);
            response.put("count", count);
            response.put("year", year);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudieron importar festivos para el año " + year);
        }
        */
    }
    
    /**
     * Genera vacaciones automáticas para todos los empleados
     */
    @PostMapping("/auto-assign-vacations")
    public ResponseEntity<?> autoAssignVacations(@RequestParam(defaultValue = "0") int year) {
        // Verificar permisos (solo admin puede asignar vacaciones automáticamente)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden asignar vacaciones automáticamente");
        }
        
        // Si no se especifica un año, usar el año actual
        if (year <= 0) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        
        // Asignar vacaciones
        int count = vacationService.assignVacationsForYear(year);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Se asignaron vacaciones a " + count + " empleados para el año " + year);
        response.put("count", count);
        response.put("year", year);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Genera vacaciones automáticas para un departamento específico
     */
    @PostMapping("/auto-assign-vacations/{departmentId}")
    public ResponseEntity<?> autoAssignVacationsForDepartment(
            @PathVariable String departmentId,
            @RequestParam(defaultValue = "0") int year) {
        
        // Verificar permisos (admin o jefe del departamento)
        boolean isAdmin = userService.isCurrentUserAdmin();
        boolean isDepartmentHead = userService.isCurrentUserDepartmentHeadOf(departmentId);
        
        if (!isAdmin && !isDepartmentHead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para asignar vacaciones a este departamento");
        }
        
        // Si no se especifica un año, usar el año actual
        if (year <= 0) {
            year = Calendar.getInstance().get(Calendar.YEAR);
        }
        
        // Asignar vacaciones
        int count = vacationService.assignVacationsForDepartment(departmentId, year);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Se asignaron vacaciones a " + count + " empleados del departamento para el año " + year);
        response.put("count", count);
        response.put("year", year);
        response.put("departmentId", departmentId);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Ejecuta el procesamiento de fin de año (importar festivos y generar vacaciones)
     * para un año específico
     */
    @PostMapping("/process-year")
    public ResponseEntity<?> processYear(@RequestParam(defaultValue = "0") int year) {
        // Verificar permisos (solo admin puede ejecutar el procesamiento de fin de año)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden ejecutar el procesamiento de fin de año");
        }
        
        // Si no se especifica un año, usar el año siguiente
        if (year <= 0) {
            year = Calendar.getInstance().get(Calendar.YEAR) + 1;
        }
        
        // Ejecutar el procesamiento
        String result = scheduledTasksService.forceYearEndProcessing(year);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Procesamiento de fin de año ejecutado para " + year);
        response.put("details", result);
        response.put("year", year);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene todos los festivos nacionales
     */
    @GetMapping("/national")
    public List<ShiftException> getNationalHolidays(
            @RequestParam(required = false) Integer year) {
        
        if (year == null || year <= 0) {
            // Si no se especifica año, devolver todos los festivos nacionales
            return shiftExceptionRepository.findByTypeAndEmployeeIdIsNull("NATIONAL_HOLIDAY");
        } else {
            // Si se especifica año, filtrar por año
            Calendar cal = Calendar.getInstance();
            cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
            java.util.Date yearStart = cal.getTime();
            
            cal.set(year + 1, Calendar.JANUARY, 1, 0, 0, 0);
            java.util.Date yearEnd = cal.getTime();
            
            return shiftExceptionRepository.findByTypeAndEmployeeIdIsNullAndDateRange("NATIONAL_HOLIDAY", yearStart, yearEnd);
        }
    }
    
    /**
     * Obtiene las vacaciones de un empleado específico
     */
    @GetMapping("/vacations/{employeeId}")
    public ResponseEntity<?> getEmployeeVacations(
            @PathVariable String employeeId,
            @RequestParam(required = false) Integer year) {
        
        // Verificar permisos (solo el propio empleado, su jefe o admin)
        boolean isAdmin = userService.isCurrentUserAdmin();
        boolean isSelf = userService.isCurrentUserEmployee(employeeId);
        boolean isDepartmentHead = userService.isCurrentUserDepartmentHeadOfEmployee(employeeId);
        
        if (!isAdmin && !isSelf && !isDepartmentHead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para ver las vacaciones de este empleado");
        }
        
        List<ShiftException> vacations;
        
        if (year == null || year <= 0) {
            // Si no se especifica año, devolver todas las vacaciones
            vacations = shiftExceptionRepository.findByEmployeeIdAndType(employeeId, "VACATION");
        } else {
            // Si se especifica año, filtrar por año
            Calendar cal = Calendar.getInstance();
            cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
            java.util.Date yearStart = cal.getTime();
            
            cal.set(year + 1, Calendar.JANUARY, 1, 0, 0, 0);
            java.util.Date yearEnd = cal.getTime();
            
            vacations = shiftExceptionRepository.findByEmployeeIdAndTypeAndDateRange(
                    employeeId, "VACATION", yearStart, yearEnd);
        }
        
        return ResponseEntity.ok(vacations);
    }
    
    /**
     * Obtiene todas las excepciones de turno de un empleado (festivos, vacaciones, etc.)
     */
    @GetMapping("/exceptions/{employeeId}")
    public ResponseEntity<?> getShiftExceptionsByEmployee(@PathVariable String employeeId) {
        // Verificar permisos (solo el propio empleado, su jefe o admin)
        boolean isAdmin = userService.isCurrentUserAdmin();
        boolean isSelf = userService.isCurrentUserEmployee(employeeId);
        boolean isDepartmentHead = userService.isCurrentUserDepartmentHeadOfEmployee(employeeId);
        
        if (!isAdmin && !isSelf && !isDepartmentHead) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para ver las excepciones de este empleado");
        }
        
        // Obtener todas las excepciones del empleado (VACATION, SICK_LEAVE, etc.)
        List<ShiftException> employeeSpecificExceptions = shiftExceptionRepository.findByEmployeeId(employeeId);
        
        // Obtener todos los festivos nacionales (NATIONAL_HOLIDAY donde employeeId es null)
        List<ShiftException> nationalHolidays = shiftExceptionRepository.findByTypeAndEmployeeIdIsNull("NATIONAL_HOLIDAY");
        
        // Combinar ambas listas
        List<ShiftException> allExceptions = new ArrayList<>(employeeSpecificExceptions);
        allExceptions.addAll(nationalHolidays);
        
        return ResponseEntity.ok(allExceptions);
    }
    
    /**
     * Endpoint de prueba para verificar la conexión con la API de Calendarific
     * 🔥 FUNCIONALIDAD DESHABILITADA - Requiere Calendarific API
     */
    @GetMapping("/test-calendarific")
    public ResponseEntity<?> testCalendarificAPI(@RequestParam(defaultValue = "2024") int year) {
        // Verificar permisos (solo admin puede probar la API)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden probar la API");
        }
        
        // 🔥 FUNCIONALIDAD DESHABILITADA
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Prueba de API Calendarific deshabilitada");
        response.put("reason", "Calendarific API no está configurada");
        response.put("status", "🚫 Deshabilitado");
        response.put("año", year);
        response.put("solution", "Para habilitar: Configura calendarific.api.key en application.properties y descomenta el código");
        
        logger.warn("🚫 Intento de prueba de Calendarific API - Funcionalidad deshabilitada");
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        
        /* 🔥 CÓDIGO COMENTADO - DESCOMENTA PARA HABILITAR CALENDARIFIC
        try {
            // Intentar importar festivos sin verificar si ya existen
            int count = calendarificService.importNationalHolidays(year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Prueba de API completada");
            response.put("festivosImportados", count);
            response.put("año", year);
            
            // Verificar cuántos festivos hay ahora en la base de datos
            List<ShiftException> allHolidays = shiftExceptionRepository.findByTypeAndEmployeeIdIsNull("NATIONAL_HOLIDAY");
            response.put("totalFestivosEnBD", allHolidays.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al conectar con la API de Calendarific");
            errorResponse.put("detalles", e.getMessage());
            errorResponse.put("año", year);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        */
    }
    
    /**
     * Fuerza la importación de festivos eliminando los existentes primero
     * 🔥 FUNCIONALIDAD DESHABILITADA - Requiere Calendarific API
     */
    @PostMapping("/force-import-holidays")
    public ResponseEntity<?> forceImportHolidays(@RequestParam(defaultValue = "2024") int year) {
        // Verificar permisos (solo admin puede forzar la importación)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden forzar la importación");
        }
        
        // 🔥 FUNCIONALIDAD DESHABILITADA
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Importación forzada de festivos deshabilitada");
        response.put("reason", "Calendarific API no está configurada");
        response.put("year", year);
        response.put("solution", "Los festivos pueden añadirse manualmente desde la interfaz de administración");
        
        logger.warn("🚫 Intento de importación forzada para año {} - Funcionalidad deshabilitada", year);
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
        
        /* 🔥 CÓDIGO COMENTADO - DESCOMENTA PARA HABILITAR CALENDARIFIC
        try {
            // Eliminar festivos existentes del año
            int deletedCount = calendarificService.deleteExistingNationalHolidays(year);
            
            // Importar nuevos festivos
            int importedCount = calendarificService.importNationalHolidays(year);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Importación forzada completada para el año " + year);
            response.put("festivosEliminados", deletedCount);
            response.put("festivosImportados", importedCount);
            response.put("año", year);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error durante la importación forzada");
            errorResponse.put("detalles", e.getMessage());
            errorResponse.put("año", year);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
        */
    }
    

} 