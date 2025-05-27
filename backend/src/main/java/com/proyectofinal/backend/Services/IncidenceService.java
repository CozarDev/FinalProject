package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.Department;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.Incidence;
import com.proyectofinal.backend.Repositories.DepartmentRepository;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.IncidenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IncidenceService {

    private static final Logger logger = LoggerFactory.getLogger(IncidenceService.class);
    private static final String INCIDENCES_DEPARTMENT_NAME = "Incidencias";

    @Autowired
    private IncidenceRepository incidenceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserService userService;

    // **MÉTODOS PARA EMPLEADOS DEL DEPARTAMENTO DE INCIDENCIAS**

    /**
     * Obtiene incidencias pendientes (para empleados de incidencias)
     * Ordenadas por prioridad: ALTA → MEDIA → BAJA
     */
    public List<Incidence> getPendingIncidencesForIncidenceEmployees() {
        logger.info("Obteniendo incidencias pendientes para empleados de incidencias");
        return incidenceRepository.findByStatusOrderByPriorityAsc(Incidence.Status.PENDIENTE);
    }

    /**
     * Acepta una incidencia (empleado de incidencias toma la incidencia)
     */
    public Incidence acceptIncidence(String incidenceId, String employeeId) {
        logger.info("Empleado {} intenta aceptar incidencia {}", employeeId, incidenceId);

        Optional<Incidence> optionalIncidence = incidenceRepository.findById(incidenceId);
        if (optionalIncidence.isEmpty()) {
            throw new RuntimeException("Incidencia no encontrada");
        }

        Incidence incidence = optionalIncidence.get();
        
        // Verificar que la incidencia puede ser aceptada
        if (!incidence.canBeAccepted()) {
            throw new RuntimeException("La incidencia no puede ser aceptada en su estado actual: " + incidence.getStatus());
        }

        // Aceptar la incidencia
        incidence.accept(employeeId);
        Incidence savedIncidence = incidenceRepository.save(incidence);

        logger.info("Incidencia {} aceptada por empleado {}", incidenceId, employeeId);
        return savedIncidence;
    }

    /**
     * Resuelve una incidencia (marca como solucionada)
     */
    public Incidence resolveIncidence(String incidenceId, String employeeId) {
        logger.info("Empleado {} intenta resolver incidencia {}", employeeId, incidenceId);

        Optional<Incidence> optionalIncidence = incidenceRepository.findById(incidenceId);
        if (optionalIncidence.isEmpty()) {
            throw new RuntimeException("Incidencia no encontrada");
        }

        Incidence incidence = optionalIncidence.get();
        
        // Verificar que es el empleado asignado quien intenta resolverla
        if (!employeeId.equals(incidence.getAssignedTo())) {
            throw new RuntimeException("Solo el empleado asignado puede resolver esta incidencia");
        }

        // Verificar que la incidencia puede ser resuelta
        if (!incidence.canBeResolved()) {
            throw new RuntimeException("La incidencia no puede ser resuelta en su estado actual: " + incidence.getStatus());
        }

        // Resolver la incidencia
        incidence.resolve();
        Incidence savedIncidence = incidenceRepository.save(incidence);

        logger.info("Incidencia {} resuelta por empleado {}", incidenceId, employeeId);
        return savedIncidence;
    }

    /**
     * Obtiene incidencias asignadas a un empleado de incidencias específico
     */
    public List<Incidence> getIncidencesAssignedToEmployee(String employeeId) {
        return incidenceRepository.findByAssignedToOrderByCreatedAtDesc(employeeId);
    }

    // **MÉTODOS PARA JEFE DE DEPARTAMENTO DE INCIDENCIAS**

    /**
     * Obtiene todas las incidencias (para jefe de departamento de incidencias)
     */
    public List<Incidence> getAllIncidencesForIncidenceManager() {
        logger.info("Obteniendo todas las incidencias para jefe de departamento de incidencias");
        return incidenceRepository.findAllOrderByPriorityAndCreatedAt();
    }

    /**
     * Obtiene incidencias activas (PENDIENTE + EN_CURSO) para jefe de incidencias
     */
    public List<Incidence> getActiveIncidencesForIncidenceManager() {
        return incidenceRepository.findActiveIncidencesOrderByPriorityAndDate();
    }

    // **MÉTODOS PARA ADMIN**

    /**
     * Obtiene todas las incidencias (para admin)
     */
    public List<Incidence> getAllIncidencesForAdmin() {
        logger.info("Obteniendo todas las incidencias para admin");
        return incidenceRepository.findAllOrderByPriorityAndCreatedAt();
    }

    /**
     * Crea una nueva incidencia (admin)
     */
    public Incidence createIncidenceAsAdmin(String title, String description, Incidence.Priority priority, String createdBy) {
        logger.info("Admin creando incidencia: {}", title);
        
        Incidence incidence = new Incidence(title, description, priority, createdBy);
        Incidence savedIncidence = incidenceRepository.save(incidence);
        
        logger.info("Incidencia creada por admin con ID: {}", savedIncidence.getId());
        return savedIncidence;
    }

    /**
     * Elimina una incidencia (admin puede eliminar cualquiera)
     */
    public void deleteIncidenceAsAdmin(String incidenceId) {
        logger.info("Admin eliminando incidencia: {}", incidenceId);
        
        Optional<Incidence> optionalIncidence = incidenceRepository.findById(incidenceId);
        if (optionalIncidence.isEmpty()) {
            throw new RuntimeException("Incidencia no encontrada");
        }
        
        incidenceRepository.deleteById(incidenceId);
        logger.info("Incidencia {} eliminada por admin", incidenceId);
    }

    // **MÉTODOS PARA EMPLEADOS DE OTROS DEPARTAMENTOS**

    /**
     * Obtiene incidencias creadas por un empleado específico
     */
    public List<Incidence> getIncidencesCreatedByEmployee(String employeeId) {
        return incidenceRepository.findByCreatedByOrderByCreatedAtDesc(employeeId);
    }

    /**
     * Crea una nueva incidencia (empleado normal)
     */
    public Incidence createIncidenceAsEmployee(String title, String description, Incidence.Priority priority, String createdBy) {
        logger.info("Empleado {} creando incidencia: {}", createdBy, title);
        
        Incidence incidence = new Incidence(title, description, priority, createdBy);
        Incidence savedIncidence = incidenceRepository.save(incidence);
        
        logger.info("Incidencia creada por empleado {} con ID: {}", createdBy, savedIncidence.getId());
        return savedIncidence;
    }

    /**
     * Elimina una incidencia pendiente (empleado puede eliminar solo las suyas y solo si están pendientes)
     */
    public void deleteIncidenceAsEmployee(String incidenceId, String employeeId) {
        logger.info("Empleado {} intentando eliminar incidencia {}", employeeId, incidenceId);
        
        Optional<Incidence> optionalIncidence = incidenceRepository.findById(incidenceId);
        if (optionalIncidence.isEmpty()) {
            throw new RuntimeException("Incidencia no encontrada");
        }
        
        Incidence incidence = optionalIncidence.get();
        
        // Verificar que es el creador
        if (!employeeId.equals(incidence.getCreatedBy())) {
            throw new RuntimeException("Solo puedes eliminar tus propias incidencias");
        }
        
        // Verificar que está pendiente
        if (!incidence.canBeDeleted()) {
            throw new RuntimeException("Solo puedes eliminar incidencias que estén pendientes");
        }
        
        incidenceRepository.deleteById(incidenceId);
        logger.info("Incidencia {} eliminada por su creador {}", incidenceId, employeeId);
    }

    // **MÉTODOS PARA JEFES DE OTROS DEPARTAMENTOS**

    /**
     * Obtiene incidencias de un departamento específico (para jefe de departamento)
     */
    public List<Incidence> getIncidencesForDepartment(String departmentId) {
        logger.info("Obteniendo incidencias del departamento: {}", departmentId);
        
        // Obtener empleados del departamento
        List<Employee> departmentEmployees = employeeRepository.findByDepartmentId(departmentId);
        List<String> employeeIds = departmentEmployees.stream()
                .map(Employee::getId)
                .collect(Collectors.toList());
        
        if (employeeIds.isEmpty()) {
            logger.info("No hay empleados en el departamento {}", departmentId);
            return List.of();
        }
        
        return incidenceRepository.findByCreatedByInOrderByCreatedAtDesc(employeeIds);
    }

    /**
     * Elimina una incidencia pendiente (jefe puede eliminar incidencias pendientes de su departamento)
     */
    public void deleteIncidenceAsDepartmentManager(String incidenceId, String managerId, String departmentId) {
        logger.info("Jefe de departamento {} intentando eliminar incidencia {}", managerId, incidenceId);
        
        Optional<Incidence> optionalIncidence = incidenceRepository.findById(incidenceId);
        if (optionalIncidence.isEmpty()) {
            throw new RuntimeException("Incidencia no encontrada");
        }
        
        Incidence incidence = optionalIncidence.get();
        
        // Verificar que está pendiente
        if (!incidence.canBeDeleted()) {
            throw new RuntimeException("Solo puedes eliminar incidencias que estén pendientes");
        }
        
        // Verificar que el creador pertenece al departamento del jefe
        Optional<Employee> optionalCreator = employeeRepository.findById(incidence.getCreatedBy());
        if (optionalCreator.isEmpty()) {
            throw new RuntimeException("Creador de la incidencia no encontrado");
        }
        
        Employee creator = optionalCreator.get();
        if (!departmentId.equals(creator.getDepartmentId())) {
            throw new RuntimeException("Solo puedes eliminar incidencias de empleados de tu departamento");
        }
        
        incidenceRepository.deleteById(incidenceId);
        logger.info("Incidencia {} eliminada por jefe de departamento {}", incidenceId, managerId);
    }

    // **MÉTODOS DE UTILIDAD**

    /**
     * Obtiene una incidencia por ID
     */
    public Optional<Incidence> getIncidenceById(String incidenceId) {
        return incidenceRepository.findById(incidenceId);
    }

    /**
     * Verifica si un empleado pertenece al departamento de incidencias
     */
    public boolean isIncidencesDepartmentEmployee(String userId) {
        try {
            // Buscar empleado por userId
            List<Employee> employees = employeeRepository.findAll();
            Employee employee = null;
            for (Employee emp : employees) {
                if (emp.getUserId() != null && emp.getUserId().equals(userId)) {
                    employee = emp;
                    break;
                }
            }
            
            if (employee == null) {
                logger.debug("No se encontró empleado con userId: {}", userId);
                return false;
            }
            
            if (employee.getDepartmentId() == null) {
                logger.debug("Empleado {} no tiene departamento asignado", employee.getId());
                return false;
            }
            
            Optional<Department> optionalDepartment = departmentRepository.findById(employee.getDepartmentId());
            if (optionalDepartment.isEmpty()) {
                logger.debug("Departamento {} no encontrado", employee.getDepartmentId());
                return false;
            }
            
            Department department = optionalDepartment.get();
            boolean isIncidencesDept = INCIDENCES_DEPARTMENT_NAME.equalsIgnoreCase(department.getName());
            
            logger.debug("Usuario {} - Empleado: {}, Departamento: {}, Es dept. incidencias: {}", 
                        userId, employee.getId(), department.getName(), isIncidencesDept);
            
            return isIncidencesDept;
            
        } catch (Exception e) {
            logger.error("Error verificando si usuario {} pertenece al departamento de incidencias", userId, e);
            return false;
        }
    }

    /**
     * Verifica si un empleado es jefe del departamento de incidencias
     */
    public boolean isIncidencesDepartmentManager(String userId) {
        try {
            // Buscar empleado por userId
            List<Employee> employees = employeeRepository.findAll();
            Employee employee = null;
            for (Employee emp : employees) {
                if (emp.getUserId() != null && emp.getUserId().equals(userId)) {
                    employee = emp;
                    break;
                }
            }
            
            if (employee == null) {
                logger.debug("No se encontró empleado con userId: {}", userId);
                return false;
            }
            
            Department incidencesDepartment = departmentRepository.findByNameIgnoreCase(INCIDENCES_DEPARTMENT_NAME);
            boolean isManager = incidencesDepartment != null && employee.getId().equals(incidencesDepartment.getManagerId());
            
            logger.debug("Usuario {} - Empleado: {}, Es jefe dept. incidencias: {}", 
                        userId, employee.getId(), isManager);
            
            return isManager;
        } catch (Exception e) {
            logger.error("Error verificando si usuario {} es jefe del departamento de incidencias", userId, e);
            return false;
        }
    }

    // **MÉTODOS DE ESTADÍSTICAS**

    /**
     * Obtiene estadísticas de incidencias
     */
    public IncidenceStats getIncidenceStats() {
        long pendingCount = incidenceRepository.countByStatus(Incidence.Status.PENDIENTE);
        long inProgressCount = incidenceRepository.countByStatus(Incidence.Status.EN_CURSO);
        long resolvedCount = incidenceRepository.countByStatus(Incidence.Status.SOLVENTADA);
        
        return new IncidenceStats(pendingCount, inProgressCount, resolvedCount);
    }

    /**
     * Clase para estadísticas de incidencias
     */
    public static class IncidenceStats {
        private final long pending;
        private final long inProgress;
        private final long resolved;

        public IncidenceStats(long pending, long inProgress, long resolved) {
            this.pending = pending;
            this.inProgress = inProgress;
            this.resolved = resolved;
        }

        public long getPending() { return pending; }
        public long getInProgress() { return inProgress; }
        public long getResolved() { return resolved; }
        public long getTotal() { return pending + inProgress + resolved; }
    }
} 