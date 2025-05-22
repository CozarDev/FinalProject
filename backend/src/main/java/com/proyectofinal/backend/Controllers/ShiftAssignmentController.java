package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.Department;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.ShiftAssignment;
import com.proyectofinal.backend.Repositories.DepartmentRepository;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.ShiftAssignmentRepository;
import com.proyectofinal.backend.Services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shiftassignments")
public class ShiftAssignmentController {

    private static final Logger logger = LoggerFactory.getLogger(ShiftAssignmentController.class);
    
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserService userService;

    @Autowired
    public ShiftAssignmentController(ShiftAssignmentRepository shiftAssignmentRepository,
                                    EmployeeRepository employeeRepository,
                                    DepartmentRepository departmentRepository,
                                    UserService userService) {
        this.shiftAssignmentRepository = shiftAssignmentRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userService = userService;
    }

    // Obtener todas las asignaciones de turnos (solo admin)
    @GetMapping
    public ResponseEntity<?> getAllShiftAssignments() {
        logger.info("Solicitando todas las asignaciones de turnos");
        
        if (!userService.isCurrentUserAdmin()) {
            logger.warn("Usuario sin permisos intenta acceder a todas las asignaciones de turnos");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo administradores pueden ver todas las asignaciones");
        }
        
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findAll();
        logger.info("Retornando {} asignaciones de turnos", assignments.size());
        return ResponseEntity.ok(assignments);
    }

    // Obtener una asignación por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getShiftAssignmentById(@PathVariable String id) {
        logger.info("Solicitando asignación de turno con ID: {}", id);
        
        Optional<ShiftAssignment> assignment = shiftAssignmentRepository.findById(id);
        
        if (!assignment.isPresent()) {
            logger.warn("No se encuentra la asignación de turno con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        // Verificar permisos
        if (!userService.isCurrentUserAdmin()) {
            String employeeId = assignment.get().getEmployeeId();
            Optional<Employee> employee = employeeRepository.findById(employeeId);
            
            if (!employee.isPresent()) {
                logger.warn("No se encuentra el empleado asociado al turno");
                return ResponseEntity.notFound().build();
            }
            
            String departmentId = employee.get().getDepartmentId();
            
            // Si es jefe de departamento, verificar que la asignación corresponda a su departamento
            if (userService.isCurrentUserDepartmentHead()) {
                if (!userService.isCurrentUserManagerOfDepartment(departmentId)) {
                    logger.warn("Jefe de departamento intentando acceder a asignación de otro departamento");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para ver esta asignación");
                }
            } 
            // Si es empleado normal, verificar que sea su propia asignación
            else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String currentUserId = auth.getName();
                
                if (!employeeId.equals(currentUserId)) {
                    logger.warn("Empleado intentando acceder a asignación de otro empleado");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para ver esta asignación");
                }
            }
        }
        
        logger.info("Retornando asignación de turno con ID: {}", id);
        return ResponseEntity.ok(assignment.get());
    }

    // Obtener asignaciones por empleado
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getShiftAssignmentsByEmployee(@PathVariable String employeeId) {
        logger.info("Solicitando asignaciones para el empleado ID: {}", employeeId);
        
        // Verificar permisos
        if (!userService.isCurrentUserAdmin()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserId = auth.getName();
            
            // Si es empleado normal, solo puede ver sus propias asignaciones
            if (!userService.isCurrentUserDepartmentHead() && !employeeId.equals(currentUserId)) {
                logger.warn("Empleado intentando acceder a asignaciones de otro empleado");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para ver estas asignaciones");
            }
            
            // Si es jefe de departamento, verificar que el empleado esté en su departamento
            if (userService.isCurrentUserDepartmentHead()) {
                Optional<Employee> employee = employeeRepository.findById(employeeId);
                
                if (!employee.isPresent()) {
                    logger.warn("No se encuentra el empleado con ID: {}", employeeId);
                    return ResponseEntity.notFound().build();
                }
                
                String departmentId = employee.get().getDepartmentId();
                
                if (!userService.isCurrentUserManagerOfDepartment(departmentId)) {
                    logger.warn("Jefe de departamento intentando acceder a asignaciones de otro departamento");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para ver estas asignaciones");
                }
            }
        }
        
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByEmployeeId(employeeId);
        logger.info("Retornando {} asignaciones para el empleado ID: {}", assignments.size(), employeeId);
        return ResponseEntity.ok(assignments);
    }

    // Obtener asignaciones por departamento
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getShiftAssignmentsByDepartment(@PathVariable String departmentId) {
        logger.info("Solicitando asignaciones para el departamento ID: {}", departmentId);
        
        // Verificar permisos
        if (!userService.isCurrentUserAdmin()) {
            // Si es jefe de departamento, verificar que sea su departamento
            if (userService.isCurrentUserDepartmentHead()) {
                if (!userService.isCurrentUserManagerOfDepartment(departmentId)) {
                    logger.warn("Jefe de departamento intentando acceder a asignaciones de otro departamento");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para ver estas asignaciones");
                }
            } else {
                // Si es empleado normal, no puede ver asignaciones por departamento
                logger.warn("Empleado intentando acceder a asignaciones de un departamento");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para ver estas asignaciones");
            }
        }
        
        // Obtener los IDs de empleados de este departamento
        List<String> employeeIds = employeeRepository.findByDepartmentId(departmentId)
                .stream()
                .map(Employee::getId)
                .collect(Collectors.toList());
        
        if (employeeIds.isEmpty()) {
            logger.info("No hay empleados en el departamento ID: {}", departmentId);
            return ResponseEntity.ok(List.of());
        }
        
        // Obtener las asignaciones para estos empleados
        List<ShiftAssignment> assignments = shiftAssignmentRepository.findByEmployeeIdIn(employeeIds);
        logger.info("Retornando {} asignaciones para el departamento ID: {}", assignments.size(), departmentId);
        return ResponseEntity.ok(assignments);
    }

    // Crear una nueva asignación de turno
    @PostMapping
    public ResponseEntity<?> createShiftAssignment(@RequestBody ShiftAssignment shiftAssignment) {
        logger.info("Creando nueva asignación de turno para empleado ID: {}", shiftAssignment.getEmployeeId());
        
        // Verificar que el empleado existe
        Optional<Employee> employee = employeeRepository.findById(shiftAssignment.getEmployeeId());
        
        if (!employee.isPresent()) {
            logger.warn("No se encuentra el empleado con ID: {}", shiftAssignment.getEmployeeId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El empleado no existe");
        }
        
        String departmentId = employee.get().getDepartmentId();
        
        // Verificar permisos
        if (!userService.isCurrentUserAdmin()) {
            // Si es jefe de departamento, verificar que el empleado esté en su departamento
            if (userService.isCurrentUserDepartmentHead()) {
                if (!userService.isCurrentUserManagerOfDepartment(departmentId)) {
                    logger.warn("Jefe de departamento intentando crear asignación para otro departamento");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para crear esta asignación");
                }
            } else {
                // Si es empleado normal, no puede crear asignaciones
                logger.warn("Empleado intentando crear una asignación");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para crear asignaciones");
            }
        }
        
        ShiftAssignment savedAssignment = shiftAssignmentRepository.save(shiftAssignment);
        logger.info("Asignación de turno creada con ID: {}", savedAssignment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedAssignment);
    }

    // Actualizar una asignación existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShiftAssignment(@PathVariable String id, @RequestBody ShiftAssignment shiftAssignmentDetails) {
        logger.info("Actualizando asignación de turno con ID: {}", id);
        
        Optional<ShiftAssignment> shiftAssignmentOpt = shiftAssignmentRepository.findById(id);
        
        if (!shiftAssignmentOpt.isPresent()) {
            logger.warn("No se encuentra la asignación de turno con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        ShiftAssignment existingAssignment = shiftAssignmentOpt.get();
        
        // Verificar que el empleado existe
        Optional<Employee> employee = employeeRepository.findById(existingAssignment.getEmployeeId());
        
        if (!employee.isPresent()) {
            logger.warn("No se encuentra el empleado con ID: {}", existingAssignment.getEmployeeId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El empleado no existe");
        }
        
        String departmentId = employee.get().getDepartmentId();
        
        // Verificar permisos
        if (!userService.isCurrentUserAdmin()) {
            // Si es jefe de departamento, verificar que el empleado esté en su departamento
            if (userService.isCurrentUserDepartmentHead()) {
                if (!userService.isCurrentUserManagerOfDepartment(departmentId)) {
                    logger.warn("Jefe de departamento intentando actualizar asignación de otro departamento");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para actualizar esta asignación");
                }
            } else {
                // Si es empleado normal, no puede actualizar asignaciones
                logger.warn("Empleado intentando actualizar una asignación");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para actualizar asignaciones");
            }
        }
        
        // Si se está cambiando el empleado, verificar permisos adicionales
        if (!existingAssignment.getEmployeeId().equals(shiftAssignmentDetails.getEmployeeId())) {
            Optional<Employee> newEmployee = employeeRepository.findById(shiftAssignmentDetails.getEmployeeId());
            
            if (!newEmployee.isPresent()) {
                logger.warn("No se encuentra el nuevo empleado con ID: {}", shiftAssignmentDetails.getEmployeeId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("El nuevo empleado no existe");
            }
            
            String newDepartmentId = newEmployee.get().getDepartmentId();
            
            // Si es jefe de departamento, verificar que el nuevo empleado esté en su departamento
            if (userService.isCurrentUserDepartmentHead() && !userService.isCurrentUserAdmin()) {
                if (!userService.isCurrentUserManagerOfDepartment(newDepartmentId)) {
                    logger.warn("Jefe de departamento intentando asignar turno a empleado de otro departamento");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para asignar turnos a empleados de otro departamento");
                }
            }
        }
        
        // Actualizar los datos
        existingAssignment.setShiftTypeId(shiftAssignmentDetails.getShiftTypeId());
        existingAssignment.setEmployeeId(shiftAssignmentDetails.getEmployeeId());
        existingAssignment.setStartDate(shiftAssignmentDetails.getStartDate());
        existingAssignment.setEndDate(shiftAssignmentDetails.getEndDate());
        
        ShiftAssignment updatedAssignment = shiftAssignmentRepository.save(existingAssignment);
        logger.info("Asignación de turno actualizada con ID: {}", updatedAssignment.getId());
        return ResponseEntity.ok(updatedAssignment);
    }

    // Eliminar una asignación
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShiftAssignment(@PathVariable String id) {
        logger.info("Eliminando asignación de turno con ID: {}", id);
        
        Optional<ShiftAssignment> shiftAssignmentOpt = shiftAssignmentRepository.findById(id);
        
        if (!shiftAssignmentOpt.isPresent()) {
            logger.warn("No se encuentra la asignación de turno con ID: {}", id);
            return ResponseEntity.notFound().build();
        }
        
        ShiftAssignment assignment = shiftAssignmentOpt.get();
        
        // Verificar que el empleado existe
        Optional<Employee> employee = employeeRepository.findById(assignment.getEmployeeId());
        
        if (!employee.isPresent()) {
            logger.warn("No se encuentra el empleado con ID: {}", assignment.getEmployeeId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El empleado no existe");
        }
        
        String departmentId = employee.get().getDepartmentId();
        
        // Verificar permisos
        if (!userService.isCurrentUserAdmin()) {
            // Si es jefe de departamento, verificar que el empleado esté en su departamento
            if (userService.isCurrentUserDepartmentHead()) {
                if (!userService.isCurrentUserManagerOfDepartment(departmentId)) {
                    logger.warn("Jefe de departamento intentando eliminar asignación de otro departamento");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("No tienes permisos para eliminar esta asignación");
                }
            } else {
                // Si es empleado normal, no puede eliminar asignaciones
                logger.warn("Empleado intentando eliminar una asignación");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("No tienes permisos para eliminar asignaciones");
            }
        }
        
        shiftAssignmentRepository.deleteById(id);
        logger.info("Asignación de turno eliminada con ID: {}", id);
        return ResponseEntity.noContent().build();
    }
} 