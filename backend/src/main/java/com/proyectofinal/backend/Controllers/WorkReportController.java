package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.WorkReport;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Requests.WorkReportRequest;
import com.proyectofinal.backend.Services.WorkReportService;
import com.proyectofinal.backend.Services.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/work-reports")
@CrossOrigin(origins = {"http://localhost:3000", "http://10.0.2.2:8080"}, allowCredentials = "true")
public class WorkReportController {

    private static final Logger logger = LoggerFactory.getLogger(WorkReportController.class);

    @Autowired
    private WorkReportService workReportService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserService userService;

    // Crear un nuevo parte de trabajo
    @PostMapping
    public ResponseEntity<?> createWorkReport(@Valid @RequestBody WorkReportRequest request) {
        try {
            String currentUserId = userService.getCurrentUserId();
            
            // Buscar el empleado por userId
            Optional<Employee> employeeOpt = employeeRepository.findByUserId(currentUserId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Empleado no encontrado"));
            }
            Employee employee = employeeOpt.get();

            WorkReport workReport = workReportService.createWorkReport(employee.getId(), request);
            return ResponseEntity.ok(workReport);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Obtener todos los partes según el rol del usuario
    @GetMapping
    public ResponseEntity<?> getWorkReports() {
        try {
            String currentUserId = userService.getCurrentUserId();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userRole = auth.getAuthorities().iterator().next().getAuthority();

            // Buscar el empleado por userId
            Optional<Employee> employeeOpt = employeeRepository.findByUserId(currentUserId);

            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Empleado no encontrado"));
            }

            Employee employee = employeeOpt.get();

            // Obtener los partes de trabajo del empleado
            List<WorkReport> workReports = workReportService.getWorkReportsForUser(employee.getId(), userRole);
            
            // Enriquecer los datos con información del empleado
            List<Map<String, Object>> enrichedReports = workReports.stream()
                    .map(this::enrichWorkReportWithEmployeeInfo)
                    .toList();

            return ResponseEntity.ok(enrichedReports);
        } catch (Exception e) {
            logger.error("Error obteniendo partes de trabajo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener los partes de trabajo"));
        }
    }

    // Obtener un parte específico por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkReportById(@PathVariable String id) {
        try {
            String currentUserId = userService.getCurrentUserId();
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userRole = auth.getAuthorities().iterator().next().getAuthority();
            
            Optional<Employee> employeeOpt = employeeRepository.findByUserId(currentUserId);
            if (employeeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Empleado no encontrado"));
            }
            Employee employee = employeeOpt.get();

            // Verificar permisos
            if (!workReportService.canEmployeeViewReport(employee.getId(), id, userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No tienes permisos para ver este parte"));
            }

            Optional<WorkReport> workReport = workReportService.getWorkReportById(id);
            if (workReport.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> enrichedReport = enrichWorkReportWithEmployeeInfo(workReport.get());
            return ResponseEntity.ok(enrichedReport);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener el parte de trabajo"));
        }
    }

    // Actualizar un parte de trabajo (solo admin)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkReport(@PathVariable String id, @Valid @RequestBody WorkReportRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userRole = auth.getAuthorities().iterator().next().getAuthority();
            
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo los administradores pueden editar partes"));
            }

            WorkReport updatedReport = workReportService.updateWorkReport(id, request);
            Map<String, Object> enrichedReport = enrichWorkReportWithEmployeeInfo(updatedReport);
            return ResponseEntity.ok(enrichedReport);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Eliminar un parte de trabajo (solo admin)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteWorkReport(@PathVariable String id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userRole = auth.getAuthorities().iterator().next().getAuthority();
            
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Solo los administradores pueden eliminar partes"));
            }

            workReportService.deleteWorkReport(id);
            return ResponseEntity.ok(Map.of("message", "Parte de trabajo eliminado correctamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Método auxiliar para enriquecer los datos del parte con información del empleado
    private Map<String, Object> enrichWorkReportWithEmployeeInfo(WorkReport workReport) {
        Map<String, Object> enrichedReport = new HashMap<>();
        enrichedReport.put("id", workReport.getId());
        enrichedReport.put("employeeId", workReport.getEmployeeId());
        enrichedReport.put("reportDate", workReport.getReportDate());
        enrichedReport.put("startTime", workReport.getStartTime());
        enrichedReport.put("endTime", workReport.getEndTime());
        enrichedReport.put("breakDuration", workReport.getBreakDuration());
        enrichedReport.put("observations", workReport.getObservations());
        enrichedReport.put("createdAt", workReport.getCreatedAt());
        enrichedReport.put("updatedAt", workReport.getUpdatedAt());

        // Agregar información del empleado
        Employee employee = employeeRepository.findById(workReport.getEmployeeId()).orElse(null);
        if (employee != null) {
            enrichedReport.put("employeeName", employee.getFirstName() + " " + employee.getLastName());
            enrichedReport.put("employeeEmail", employee.getEmail());
        } else {
            enrichedReport.put("employeeName", "Empleado no encontrado");
            enrichedReport.put("employeeEmail", "");
        }

        return enrichedReport;
    }
} 