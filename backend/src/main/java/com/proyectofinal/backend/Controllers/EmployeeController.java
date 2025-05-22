package com.proyectofinal.backend.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Repositories.UserRepository;
import com.proyectofinal.backend.Services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public EmployeeController(EmployeeRepository employeeRepository, UserRepository userRepository, 
                            PasswordEncoder passwordEncoder, UserService userService) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    // Devuelve una lista de todos los empleados
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }
    
    // Devuelve información del usuario asociado a un empleado
    @GetMapping("/{id}/user-info")
    public ResponseEntity<?> getEmployeeUserInfo(@PathVariable String id) {
        // Solo los administradores o jefes de departamento pueden ver esta información
        if (!userService.isCurrentUserAdmin() && !userService.isCurrentUserDepartmentHead()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado");
        }
        
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (!employeeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Employee employee = employeeOpt.get();
        if (employee.getUserId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Este empleado no tiene un usuario asociado");
        }
        
        Optional<User> userOpt = userRepository.findById(employee.getUserId());
        if (!userOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No se encontró el usuario asociado al empleado");
        }
        
        User user = userOpt.get();
        
        // Para el usuario admin, enviar datos dummy por seguridad
        if ("admin".equals(user.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("username", user.getUsername());
            response.put("password", "********");
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);
        }
        
        // Para usuarios normales, enviar los datos reales
        // NOTA: En un entorno real, nunca se debería hacer esto
        Map<String, String> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("password", "defaultPassword123");  // Usamos la contraseña por defecto para todos los usuarios
        response.put("role", user.getRole());
        
        return ResponseEntity.ok(response);
    }

    // Crea un nuevo empleado y un usuario asociado, luego guarda ambos en la base de datos
    @PostMapping
    public Employee createEmployee(@RequestBody Employee employee) {
        // Generar nombre de usuario basado en el nombre y apellido
        String username = employee.getFirstName().toLowerCase() + "." + employee.getLastName().toLowerCase();
        // Generar una contraseña aleatoria
        String password = "defaultPassword123"; // Puedes usar una librería para generar contraseñas aleatorias

        // Crear y guardar el usuario
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setRole("EMPLEADO"); // Asignar rol de empleado por defecto
        userRepository.save(newUser);

        // Asignar el ID del usuario al empleado
        employee.setUserId(newUser.getId());

        return employeeRepository.save(employee);
    }

    // Actualiza un empleado existente basado en su ID
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable String id, @RequestBody Employee employeeDetails) {
        // Verificar si es empleado admin (empleado0)
        if (isAdminEmployee(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("El usuario administrador no puede ser modificado");
        }
        
        Optional<Employee> employeeOpt = employeeRepository.findById(id);
        if (!employeeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Employee existingEmployee = employeeOpt.get();
        
        // Verificar permisos según rol
        boolean isAdmin = userService.isCurrentUserAdmin();
        boolean isDeptHead = userService.isCurrentUserDepartmentHead();
        boolean isSameEmployee = userService.isCurrentUserEmployee(id);
        
        if (!isAdmin && !isDeptHead && !isSameEmployee) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permiso para modificar este empleado");
        }
        
        // Actualizar datos personales (todos los roles pueden hacer esto)
        existingEmployee.setFirstName(employeeDetails.getFirstName());
        existingEmployee.setLastName(employeeDetails.getLastName());
        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setPhone(employeeDetails.getPhone());
        
        // Solo admin o jefe de departamento pueden cambiar el departamento
        if ((isAdmin || isDeptHead) && employeeDetails.getDepartmentId() != null) {
            existingEmployee.setDepartmentId(employeeDetails.getDepartmentId());
        }
        
        // Solo el admin puede cambiar el userId (generalmente no debería ser necesario)
        if (isAdmin && employeeDetails.getUserId() != null) {
            existingEmployee.setUserId(employeeDetails.getUserId());
        }
        
        // Guardar el empleado actualizado
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        
        // Actualizar el usuario asociado si corresponde
        if ((isAdmin || isDeptHead) && existingEmployee.getUserId() != null) {
            // Obtener información del usuario desde el request usando transient fields
            String username = null;
            String password = null;
            
            // Extraer los campos del objeto JSON original (ya que Employee no tiene estos getters)
            // Este enfoque es un workaround para manejar campos transient
            try {
                // Verificamos si se recibieron estos campos como parte de la solicitud
                if (employeeDetails instanceof Map) {
                    Map<String, Object> employeeMap = (Map<String, Object>) employeeDetails;
                    if (employeeMap.containsKey("username")) {
                        username = (String) employeeMap.get("username");
                    }
                    if (employeeMap.containsKey("password")) {
                        password = (String) employeeMap.get("password");
                    }
                }
            } catch (Exception e) {
                // Si hay error en la conversión, ignoramos
                System.out.println("Error al extraer username/password: " + e.getMessage());
            }
            
            // Si se obtuvieron los datos, actualizar el usuario
            if (username != null || password != null) {
                Optional<User> userOpt = userRepository.findById(existingEmployee.getUserId());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    if (username != null) {
                        user.setUsername(username);
                    }
                    
                    if (password != null && !password.isEmpty()) {
                        user.setPassword(passwordEncoder.encode(password));
                    }
                    
                    userRepository.save(user);
                }
            }
        }
        
        return ResponseEntity.ok(updatedEmployee);
    }

    // Elimina un empleado basado en su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteEmployee(@PathVariable String id) {
        // Verificar si es empleado admin (empleado0)
        if (isAdminEmployee(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("El usuario administrador no puede ser eliminado");
        }
        
        // Solo admin o jefe de departamento pueden eliminar empleados
        if (!userService.isCurrentUserAdmin() && !userService.isCurrentUserDepartmentHead()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permiso para eliminar empleados");
        }
        
        if (employeeRepository.existsById(id)) {
            // Obtener el userId antes de eliminar el empleado
            Optional<Employee> employeeOpt = employeeRepository.findById(id);
            if (employeeOpt.isPresent() && employeeOpt.get().getUserId() != null) {
                String userId = employeeOpt.get().getUserId();
                
                // Eliminar el empleado
                employeeRepository.deleteById(id);
                
                // Eliminar el usuario asociado
                userRepository.deleteById(userId);
            } else {
                // Solo eliminar el empleado si no tiene usuario asociado
                employeeRepository.deleteById(id);
            }
            
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Verifica si el empleado es el administrador (empleado0)
    private boolean isAdminEmployee(String employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (!employeeOpt.isPresent()) {
            return false;
        }
        
        Employee employee = employeeOpt.get();
        Optional<User> userOpt = userRepository.findById(employee.getUserId());
        
        return userOpt.isPresent() && "admin".equals(userOpt.get().getUsername());
    }
} 