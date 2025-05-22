package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.ShiftException;
import com.proyectofinal.backend.Repositories.ShiftExceptionRepository;
import com.proyectofinal.backend.Services.CalendarificService;
import com.proyectofinal.backend.Services.ScheduledTasksService;
import com.proyectofinal.backend.Services.UserService;
import com.proyectofinal.backend.Services.VacationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
} 