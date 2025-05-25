package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.Incidence;
import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Services.IncidenceService;
import com.proyectofinal.backend.Services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/incidences")
public class IncidenceController {

    private static final Logger logger = LoggerFactory.getLogger(IncidenceController.class);

    @Autowired
    private IncidenceService incidenceService;

    @Autowired
    private UserService userService;

    // **ENDPOINTS PARA EMPLEADOS DEL DEPARTAMENTO DE INCIDENCIAS**

    /**
     * Obtiene incidencias pendientes (solo para empleados de incidencias)
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingIncidences() {
        String currentUserId = userService.getCurrentUserId();
        
        // Verificar permisos de acceso
        
        // Verificar que es empleado del departamento de incidencias
        if (!incidenceService.isIncidencesDepartmentEmployee(currentUserId) && 
            !incidenceService.isIncidencesDepartmentManager(currentUserId) &&
            !userService.isCurrentUserAdmin()) {
            logger.warn("Acceso denegado a pendientes. Usuario {} no tiene permisos", currentUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo empleados del departamento de incidencias pueden ver incidencias pendientes");
        }

        try {
            List<Incidence> pendingIncidences = incidenceService.getPendingIncidencesForIncidenceEmployees();
            return ResponseEntity.ok(pendingIncidences);
        } catch (Exception e) {
            logger.error("Error obteniendo incidencias pendientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo incidencias pendientes: " + e.getMessage());
        }
    }

    /**
     * Acepta una incidencia (empleado de incidencias toma la incidencia)
     */
    @PostMapping("/{incidenceId}/accept")
    public ResponseEntity<?> acceptIncidence(@PathVariable String incidenceId) {
        String currentUserId = userService.getCurrentUserId();
        
        // Verificar que es empleado del departamento de incidencias
        if (!incidenceService.isIncidencesDepartmentEmployee(currentUserId) && 
            !incidenceService.isIncidencesDepartmentManager(currentUserId) &&
            !userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo empleados del departamento de incidencias pueden aceptar incidencias");
        }

        try {
            Incidence acceptedIncidence = incidenceService.acceptIncidence(incidenceId, currentUserId);
            return ResponseEntity.ok(acceptedIncidence);
        } catch (Exception e) {
            logger.error("Error aceptando incidencia {}", incidenceId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error aceptando incidencia: " + e.getMessage());
        }
    }

    /**
     * Resuelve una incidencia (marca como solucionada)
     */
    @PostMapping("/{incidenceId}/resolve")
    public ResponseEntity<?> resolveIncidence(@PathVariable String incidenceId) {
        String currentUserId = userService.getCurrentUserId();
        
        // Verificar que es empleado del departamento de incidencias
        if (!incidenceService.isIncidencesDepartmentEmployee(currentUserId) && 
            !incidenceService.isIncidencesDepartmentManager(currentUserId) &&
            !userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo empleados del departamento de incidencias pueden resolver incidencias");
        }

        try {
            Incidence resolvedIncidence = incidenceService.resolveIncidence(incidenceId, currentUserId);
            return ResponseEntity.ok(resolvedIncidence);
        } catch (Exception e) {
            logger.error("Error resolviendo incidencia {}", incidenceId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error resolviendo incidencia: " + e.getMessage());
        }
    }

    /**
     * Obtiene incidencias asignadas al empleado actual
     */
    @GetMapping("/assigned-to-me")
    public ResponseEntity<?> getIncidencesAssignedToMe() {
        String currentUserId = userService.getCurrentUserId();
        
        // Verificar que es empleado del departamento de incidencias
        if (!incidenceService.isIncidencesDepartmentEmployee(currentUserId) && 
            !incidenceService.isIncidencesDepartmentManager(currentUserId) &&
            !userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo empleados del departamento de incidencias pueden ver incidencias asignadas");
        }

        try {
            List<Incidence> assignedIncidences = incidenceService.getIncidencesAssignedToEmployee(currentUserId);
            return ResponseEntity.ok(assignedIncidences);
        } catch (Exception e) {
            logger.error("Error obteniendo incidencias asignadas al usuario {}", currentUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo incidencias asignadas: " + e.getMessage());
        }
    }

    // **ENDPOINTS PARA JEFE DE DEPARTAMENTO DE INCIDENCIAS Y ADMIN**

    /**
     * Obtiene todas las incidencias (jefe de incidencias y admin)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllIncidences() {
        String currentUserId = userService.getCurrentUserId();
        
        // Verificar permisos
        boolean isIncidenceManager = incidenceService.isIncidencesDepartmentManager(currentUserId);
        boolean isAdmin = userService.isCurrentUserAdmin();
        
        if (!isIncidenceManager && !isAdmin) {
            logger.warn("Acceso denegado. Usuario {} no tiene permisos para ver todas las incidencias", currentUserId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo el jefe del departamento de incidencias y administradores pueden ver todas las incidencias");
        }

        try {
            List<Incidence> allIncidences;
            if (isAdmin) {
                allIncidences = incidenceService.getAllIncidencesForAdmin();
            } else {
                allIncidences = incidenceService.getAllIncidencesForIncidenceManager();
            }
            return ResponseEntity.ok(allIncidences);
        } catch (Exception e) {
            logger.error("Error obteniendo todas las incidencias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo todas las incidencias: " + e.getMessage());
        }
    }

    /**
     * Obtiene incidencias activas (jefe de incidencias y admin)
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveIncidences() {
        String currentUserId = userService.getCurrentUserId();
        
        // Verificar permisos
        boolean isIncidenceManager = incidenceService.isIncidencesDepartmentManager(currentUserId);
        boolean isAdmin = userService.isCurrentUserAdmin();
        
        if (!isIncidenceManager && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo el jefe del departamento de incidencias y administradores pueden ver incidencias activas");
        }

        try {
            List<Incidence> activeIncidences = incidenceService.getActiveIncidencesForIncidenceManager();
            return ResponseEntity.ok(activeIncidences);
        } catch (Exception e) {
            logger.error("Error obteniendo incidencias activas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo incidencias activas: " + e.getMessage());
        }
    }

    // **ENDPOINTS PARA ADMIN (CREAR Y ELIMINAR)**

    /**
     * Crea una nueva incidencia (solo admin)
     */
    @PostMapping
    public ResponseEntity<?> createIncidence(@RequestBody CreateIncidenceRequest request) {
        // Verificar que es admin
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo los administradores pueden crear incidencias directamente");
        }

        try {
            // Validar entrada
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El título es obligatorio");
            }
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La descripción es obligatoria");
            }
            if (request.getPriority() == null) {
                return ResponseEntity.badRequest().body("La prioridad es obligatoria");
            }
            if (request.getCreatedBy() == null || request.getCreatedBy().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El creador es obligatorio");
            }

            Incidence newIncidence = incidenceService.createIncidenceAsAdmin(
                    request.getTitle().trim(),
                    request.getDescription().trim(),
                    request.getPriority(),
                    request.getCreatedBy().trim()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(newIncidence);
        } catch (Exception e) {
            logger.error("Error creando incidencia", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creando incidencia: " + e.getMessage());
        }
    }

    /**
     * Elimina una incidencia (solo admin puede eliminar cualquiera)
     */
    @DeleteMapping("/{incidenceId}")
    public ResponseEntity<?> deleteIncidence(@PathVariable String incidenceId) {
        String currentUserId = userService.getCurrentUserId();
        
        try {
            // Si es admin, puede eliminar cualquier incidencia
            if (userService.isCurrentUserAdmin()) {
                incidenceService.deleteIncidenceAsAdmin(incidenceId);
                return ResponseEntity.ok().body("Incidencia eliminada exitosamente");
            }
            
            // Si es jefe de departamento, puede eliminar incidencias pendientes de su departamento
            String currentUserDepartmentId = userService.getCurrentUserDepartmentId();
            if (currentUserDepartmentId != null && userService.isCurrentUserDepartmentHead()) {
                incidenceService.deleteIncidenceAsDepartmentManager(incidenceId, currentUserId, currentUserDepartmentId);
                return ResponseEntity.ok().body("Incidencia eliminada exitosamente");
            }
            
            // Si es empleado normal, puede eliminar solo sus incidencias pendientes
            incidenceService.deleteIncidenceAsEmployee(incidenceId, currentUserId);
            return ResponseEntity.ok().body("Incidencia eliminada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error eliminando incidencia {}", incidenceId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error eliminando incidencia: " + e.getMessage());
        }
    }

    // **ENDPOINTS PARA EMPLEADOS NORMALES**

    /**
     * Obtiene incidencias creadas por el empleado actual
     */
    @GetMapping("/my-incidences")
    public ResponseEntity<?> getMyIncidences() {
        String currentUserId = userService.getCurrentUserId();

        try {
            List<Incidence> myIncidences = incidenceService.getIncidencesCreatedByEmployee(currentUserId);
            return ResponseEntity.ok(myIncidences);
        } catch (Exception e) {
            logger.error("Error obteniendo incidencias del usuario {}", currentUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo tus incidencias: " + e.getMessage());
        }
    }

    /**
     * Crea una nueva incidencia (empleado normal)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createMyIncidence(@RequestBody CreateIncidenceRequest request) {
        String currentUserId = userService.getCurrentUserId();

        try {
            // Validar entrada
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El título es obligatorio");
            }
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("La descripción es obligatoria");
            }
            if (request.getPriority() == null) {
                return ResponseEntity.badRequest().body("La prioridad es obligatoria");
            }

            Incidence newIncidence = incidenceService.createIncidenceAsEmployee(
                    request.getTitle().trim(),
                    request.getDescription().trim(),
                    request.getPriority(),
                    currentUserId
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(newIncidence);
        } catch (Exception e) {
            logger.error("Error creando incidencia del usuario {}", currentUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creando incidencia: " + e.getMessage());
        }
    }

    // **ENDPOINTS PARA JEFES DE DEPARTAMENTO**

    /**
     * Obtiene incidencias del departamento (para jefes de departamento)
     */
    @GetMapping("/department")
    public ResponseEntity<?> getDepartmentIncidences() {
        String currentUserId = userService.getCurrentUserId();
        String departmentId = userService.getCurrentUserDepartmentId();
        
        // Verificar que es jefe de departamento
        if (!userService.isCurrentUserDepartmentHead() && !userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo jefes de departamento pueden ver incidencias del departamento");
        }

        try {
            List<Incidence> departmentIncidences = incidenceService.getIncidencesForDepartment(departmentId);
            return ResponseEntity.ok(departmentIncidences);
        } catch (Exception e) {
            logger.error("Error obteniendo incidencias del departamento {}", departmentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo incidencias del departamento: " + e.getMessage());
        }
    }

    // **ENDPOINTS DE UTILIDAD**

    /**
     * Obtiene una incidencia específica por ID
     */
    @GetMapping("/{incidenceId}")
    public ResponseEntity<?> getIncidenceById(@PathVariable String incidenceId) {
        String currentUserId = userService.getCurrentUserId();

        try {
            Optional<Incidence> optionalIncidence = incidenceService.getIncidenceById(incidenceId);
            if (optionalIncidence.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Incidence incidence = optionalIncidence.get();
            
            // Verificar permisos: solo puede ver la incidencia si:
            // 1. Es admin
            // 2. Es jefe/empleado del departamento de incidencias
            // 3. Es el creador de la incidencia
            // 4. Es jefe del departamento del creador
            // 5. Está asignada a él
            
            boolean canView = userService.isCurrentUserAdmin() ||
                            incidenceService.isIncidencesDepartmentEmployee(currentUserId) ||
                            incidenceService.isIncidencesDepartmentManager(currentUserId) ||
                            currentUserId.equals(incidence.getCreatedBy()) ||
                            currentUserId.equals(incidence.getAssignedTo()) ||
                            userService.isCurrentUserDepartmentHeadOfEmployee(incidence.getCreatedBy());

            if (!canView) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para ver esta incidencia");
            }

            return ResponseEntity.ok(incidence);
        } catch (Exception e) {
            logger.error("Error obteniendo incidencia {}", incidenceId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo incidencia: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadísticas de incidencias
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getIncidenceStats() {
        // Solo admin y jefe de incidencias pueden ver estadísticas
        String currentUserId = userService.getCurrentUserId();
        boolean isIncidenceManager = incidenceService.isIncidencesDepartmentManager(currentUserId);
        boolean isAdmin = userService.isCurrentUserAdmin();
        
        if (!isIncidenceManager && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo el jefe del departamento de incidencias y administradores pueden ver estadísticas");
        }

        try {
            IncidenceService.IncidenceStats stats = incidenceService.getIncidenceStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas de incidencias", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error obteniendo estadísticas: " + e.getMessage());
        }
    }



    // **CLASES DE REQUEST**

    public static class CreateIncidenceRequest {
        private String title;
        private String description;
        private Incidence.Priority priority;
        private String createdBy; // Solo para admin

        // Getters y setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Incidence.Priority getPriority() { return priority; }
        public void setPriority(Incidence.Priority priority) { this.priority = priority; }

        public String getCreatedBy() { return createdBy; }
        public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    }
} 