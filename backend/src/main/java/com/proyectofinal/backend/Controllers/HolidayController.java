package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.ShiftException;
import com.proyectofinal.backend.Repositories.ShiftExceptionRepository;
import com.proyectofinal.backend.Services.CalendarificService;
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

    private final CalendarificService calendarificService;
    private final VacationService vacationService;
    private final ShiftExceptionRepository shiftExceptionRepository;
    private final UserService userService;
    private final ScheduledTasksService scheduledTasksService;
    private static final Logger logger = LoggerFactory.getLogger(HolidayController.class);

    public HolidayController(
            CalendarificService calendarificService,
            VacationService vacationService,
            ShiftExceptionRepository shiftExceptionRepository,
            UserService userService,
            ScheduledTasksService scheduledTasksService) {
        this.calendarificService = calendarificService;
        this.vacationService = vacationService;
        this.shiftExceptionRepository = shiftExceptionRepository;
        this.userService = userService;
        this.scheduledTasksService = scheduledTasksService;
    }

    /**
     * Importa festivos nacionales para un año específico
     */
    @PostMapping("/import-national")
    public ResponseEntity<?> importNationalHolidays(@RequestParam(defaultValue = "0") int year) {
        // Verificar permisos (solo admin puede importar festivos)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden importar festivos");
        }
        
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
     */
    @GetMapping("/test-calendarific")
    public ResponseEntity<?> testCalendarificAPI(@RequestParam(defaultValue = "2024") int year) {
        // Verificar permisos (solo admin puede probar la API)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden probar la API");
        }
        
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
    }
    
    /**
     * Fuerza la importación de festivos eliminando los existentes primero
     */
    @PostMapping("/force-import-holidays")
    public ResponseEntity<?> forceImportHolidays(@RequestParam(defaultValue = "2024") int year) {
        // Verificar permisos (solo admin puede forzar la importación)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden forzar la importación");
        }
        
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
    }
    
    /**
     * ENDPOINT TEMPORAL DE DEBUG - Fuerza la importación de festivos sin autenticación
     * ¡ELIMINAR EN PRODUCCIÓN!
     */
    @PostMapping("/debug-import-holidays")
    public ResponseEntity<?> debugImportHolidays(@RequestParam(defaultValue = "2024") int year) {
        try {
            logger.info("DEBUG: Iniciando importación de festivos para el año {}", year);
            
            // Verificar conexión a MongoDB
            long totalExceptions = shiftExceptionRepository.count();
            logger.info("DEBUG: Total de excepciones en BD antes de importar: {}", totalExceptions);
            
            // Eliminar festivos existentes del año
            int deletedCount = calendarificService.deleteExistingNationalHolidays(year);
            logger.info("DEBUG: Festivos eliminados: {}", deletedCount);
            
            // Importar nuevos festivos
            int importedCount = calendarificService.importNationalHolidays(year);
            logger.info("DEBUG: Festivos importados: {}", importedCount);
            
            // Verificar cuántos festivos hay ahora
            List<ShiftException> allHolidays = shiftExceptionRepository.findByTypeAndEmployeeIdIsNull("NATIONAL_HOLIDAY");
            logger.info("DEBUG: Total de festivos en BD después de importar: {}", allHolidays.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "DEBUG: Importación completada para el año " + year);
            response.put("festivosEliminados", deletedCount);
            response.put("festivosImportados", importedCount);
            response.put("totalFestivosEnBD", allHolidays.size());
            response.put("totalExcepcionesBD", totalExceptions);
            response.put("año", year);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("DEBUG: Error durante la importación", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error durante la importación de debug");
            errorResponse.put("detalles", e.getMessage());
            errorResponse.put("stackTrace", e.getStackTrace());
            errorResponse.put("año", year);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * ENDPOINT TEMPORAL DE DEBUG - Prueba directa de la API de Calendarific
     * ¡ELIMINAR EN PRODUCCIÓN!
     */
    @GetMapping("/debug-test-api")
    public ResponseEntity<?> debugTestCalendarificAPI(@RequestParam(defaultValue = "2024") int year) {
        try {
            logger.info("DEBUG: Probando API de Calendarific directamente para el año {}", year);
            
            // Construir la URL exactamente como lo hace el servicio
            String apiKey = "ijRMfLATKCEWWDkl7CkSuO9v9D6U6nCX"; // Hardcoded para debug
            String url = "https://calendarific.com/api/v2/holidays?api_key=" + apiKey + "&country=ES&year=" + year;
            
            logger.info("DEBUG: URL de la API: {}", url.replace(apiKey, "***"));
            
            // Hacer la llamada directa
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            String rawResponse = restTemplate.getForObject(url, String.class);
            
            logger.info("DEBUG: Respuesta cruda de la API: {}", rawResponse);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "DEBUG: Prueba directa de API completada");
            response.put("url", url.replace(apiKey, "***"));
            response.put("rawResponse", rawResponse);
            response.put("año", year);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("DEBUG: Error al probar API directamente", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error al probar API de Calendarific directamente");
            errorResponse.put("detalles", e.getMessage());
            errorResponse.put("causa", e.getCause() != null ? e.getCause().getMessage() : "Sin causa específica");
            errorResponse.put("año", year);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * ENDPOINT SUPER SIMPLE - Importar festivos con GET (fácil de probar desde navegador)
     * ¡ELIMINAR EN PRODUCCIÓN!
     */
    @GetMapping("/simple-import")
    public ResponseEntity<?> simpleImport(@RequestParam(defaultValue = "2024") int year) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("SIMPLE IMPORT: Iniciando para año {}", year);
            
            // Verificar MongoDB primero
            long totalBefore = shiftExceptionRepository.count();
            logger.info("SIMPLE IMPORT: Total excepciones en BD antes: {}", totalBefore);
            response.put("totalExcepcionesBD_antes", totalBefore);
            
            // Intentar importar
            logger.info("SIMPLE IMPORT: Llamando a calendarificService.importNationalHolidays()...");
            int imported = calendarificService.importNationalHolidays(year);
            logger.info("SIMPLE IMPORT: Festivos importados: {}", imported);
            response.put("festivosImportados", imported);
            
            // Verificar después
            long totalAfter = shiftExceptionRepository.count();
            List<ShiftException> holidays = shiftExceptionRepository.findByTypeAndEmployeeIdIsNull("NATIONAL_HOLIDAY");
            
            logger.info("SIMPLE IMPORT: Total excepciones después: {}", totalAfter);
            logger.info("SIMPLE IMPORT: Total festivos nacionales: {}", holidays.size());
            
            response.put("totalExcepcionesBD_despues", totalAfter);
            response.put("totalFestivosNacionales", holidays.size());
            response.put("año", year);
            response.put("status", "SUCCESS");
            response.put("message", "Importación completada. Ver logs del servidor para detalles.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("SIMPLE IMPORT: Error durante importación", e);
            
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            response.put("causa", e.getCause() != null ? e.getCause().getMessage() : "Sin causa específica");
            response.put("año", year);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 