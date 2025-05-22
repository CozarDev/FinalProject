package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.Department;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Repositories.DepartmentRepository;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.UserRepository;
import com.proyectofinal.backend.Services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public DepartmentController(DepartmentRepository departmentRepository, 
                               EmployeeRepository employeeRepository,
                               UserRepository userRepository,
                               UserService userService) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // Devuelve una lista de todos los departamentos
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Crea un nuevo departamento y lo guarda en la base de datos
    @PostMapping
    public Department createDepartment(@RequestBody Department department) {
        return departmentRepository.save(department);
    }

    // Actualiza un departamento existente basado en su ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable String id, @RequestBody Department departmentDetails) {
        // Verificar permisos (solo admin puede actualizar departamentos)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el administrador puede actualizar departamentos");
        }
        
        Optional<Department> departmentOpt = departmentRepository.findById(id);
        if (!departmentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Department department = departmentOpt.get();
        department.setName(departmentDetails.getName());
        department.setDescription(departmentDetails.getDescription());
        
        // Manejar asignación de jefe de departamento
        String newManagerId = departmentDetails.getManagerId();
        if (newManagerId != null && !newManagerId.equals(department.getManagerId())) {
            // Verificar si el empleado existe
            Optional<Employee> employeeOpt = employeeRepository.findById(newManagerId);
            if (!employeeOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("El empleado seleccionado como jefe no existe");
            }
            
            Employee manager = employeeOpt.get();
            
            // Cambiar el rol del usuario a "DEPARTMENT_HEAD"
            if (manager.getUserId() != null) {
                Optional<User> userOpt = userRepository.findById(manager.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setRole("DEPARTMENT_HEAD");
                    userRepository.save(user);
                    
                    // Si el empleado no está en este departamento, actualizarlo
                    if (!id.equals(manager.getDepartmentId())) {
                        manager.setDepartmentId(id);
                        employeeRepository.save(manager);
                    }
                }
            }
            
            // Actualizar el managerId del departamento
            department.setManagerId(newManagerId);
        }
        
        return ResponseEntity.ok(departmentRepository.save(department));
    }

    // Asigna un jefe a un departamento
    @PostMapping("/{departmentId}/assign-manager")
    public ResponseEntity<?> assignDepartmentManager(
            @PathVariable String departmentId,
            @RequestBody Map<String, String> request) {
        
        // Verificar permisos (solo admin puede asignar jefes)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Solo el administrador puede asignar jefes de departamento");
        }
        
        // Obtener el departamento
        Optional<Department> departmentOpt = departmentRepository.findById(departmentId);
        if (!departmentOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // Obtener el ID del empleado que será jefe
        String employeeId = request.get("employeeId");
        if (employeeId == null || employeeId.isEmpty()) {
            return ResponseEntity.badRequest().body("Se requiere un ID de empleado válido");
        }
        
        // Verificar si el empleado existe
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (!employeeOpt.isPresent()) {
            return ResponseEntity.badRequest().body("El empleado no existe");
        }
        
        Employee employee = employeeOpt.get();
        Department department = departmentOpt.get();
        
        // Actualizar el departamento con el nuevo jefe
        department.setManagerId(employeeId);
        departmentRepository.save(department);
        
        // Actualizar el rol del usuario a DEPARTMENT_HEAD
        if (employee.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(employee.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRole("DEPARTMENT_HEAD");
                userRepository.save(user);
                
                // Si el empleado no está en este departamento, actualizarlo
                if (!departmentId.equals(employee.getDepartmentId())) {
                    employee.setDepartmentId(departmentId);
                    employeeRepository.save(employee);
                }
            }
        }
        
        return ResponseEntity.ok().body(Map.of(
            "message", "Jefe de departamento asignado correctamente",
            "department", department
        ));
    }

    // Elimina un departamento basado en su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Busca empleados para asignar como jefe de departamento
    @GetMapping("/search-employees")
    public ResponseEntity<?> searchEmployees(@RequestParam String query) {
        // Implementar búsqueda de empleados por nombre o apellido
        List<Employee> employees = employeeRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            query, query);
        return ResponseEntity.ok(employees);
    }
} 