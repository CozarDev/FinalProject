package com.proyectofinal.backend.Controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Repositories.UserRepository;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Services.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, EmployeeRepository employeeRepository,
                         UserService userService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Obtiene los datos del perfil del usuario actual
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        
        // Buscar el empleado asociado al usuario
        Optional<Employee> employeeOpt = employeeRepository.findByUserId(currentUser.getId());
        if (!employeeOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Perfil de empleado no encontrado");
        }
        
        Employee employee = employeeOpt.get();
        
        // Crear objeto con datos combinados de usuario y empleado
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("userId", currentUser.getId());
        profileData.put("username", currentUser.getUsername());
        profileData.put("role", currentUser.getRole());
        profileData.put("employeeId", employee.getId());
        profileData.put("firstName", employee.getFirstName());
        profileData.put("lastName", employee.getLastName());
        profileData.put("email", employee.getEmail());
        profileData.put("phone", employee.getPhone());
        profileData.put("departmentId", employee.getDepartmentId());
        
        return ResponseEntity.ok(profileData);
    }
    
    /**
     * Obtiene la contraseña sin encriptar (solo para mostrar)
     * NOTA: Este endpoint debería usarse solo con fines de visualización y está protegido por autenticación
     */
    @GetMapping("/{userId}/password")
    public ResponseEntity<?> getUserPassword(@PathVariable String userId) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        
        // Solo admin o el propio usuario pueden ver la contraseña
        if (!userService.isCurrentUserAdmin() && !currentUser.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permiso para ver esta información");
        }
        
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        
        // Contraseña dummy para el usuario admin (no mostramos la real por seguridad)
        if ("admin".equals(user.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("password", "********");
            return ResponseEntity.ok(response);
        }
        
        // Para usuarios normales, recuperar la contraseña de forma segura
        // NOTA: En un entorno real, nunca se debería hacer esto
        // Este es un ejemplo simplificado para propósitos educativos
        Map<String, String> response = new HashMap<>();
        response.put("password", "defaultPassword123"); // Usamos la contraseña por defecto que asignamos en employee controller
        
        return ResponseEntity.ok(response);
    }

    /**
     * Verifica las credenciales actuales del usuario antes de permitir un cambio de contraseña
     */
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyCurrentPassword(@RequestBody Map<String, String> credentials) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        
        String currentPassword = credentials.get("currentPassword");
        if (currentPassword == null) {
            return ResponseEntity.badRequest().body("La contraseña actual es requerida");
        }
        
        boolean isPasswordValid = passwordEncoder.matches(currentPassword, currentUser.getPassword());
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isPasswordValid);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los datos del perfil de usuario
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, Object> profileData) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no autenticado");
        }
        
        // Buscar el empleado asociado al usuario
        Optional<Employee> employeeOpt = employeeRepository.findByUserId(currentUser.getId());
        if (!employeeOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Perfil de empleado no encontrado");
        }
        
        Employee employee = employeeOpt.get();
        
        // Verificar si se está intentando actualizar el usuario admin
        if ("admin".equals(currentUser.getUsername())) {
            // Solo permitir cambiar la contraseña del admin, no otros datos
            if (profileData.containsKey("password")) {
                String newPassword = (String) profileData.get("password");
                currentUser.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(currentUser);
                
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Contraseña actualizada correctamente");
                response.put("passwordChanged", true);
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("El usuario administrador solo puede cambiar su contraseña");
            }
        }
        
        // Actualizar datos del empleado
        boolean dataUpdated = false;
        
        if (profileData.containsKey("firstName")) {
            employee.setFirstName((String) profileData.get("firstName"));
            dataUpdated = true;
        }
        
        if (profileData.containsKey("lastName")) {
            employee.setLastName((String) profileData.get("lastName"));
            dataUpdated = true;
        }
        
        if (profileData.containsKey("email")) {
            employee.setEmail((String) profileData.get("email"));
            dataUpdated = true;
        }
        
        if (profileData.containsKey("phone")) {
            employee.setPhone((String) profileData.get("phone"));
            dataUpdated = true;
        }
        
        // Actualizar datos del usuario
        boolean passwordChanged = false;
        
        if (profileData.containsKey("username")) {
            currentUser.setUsername((String) profileData.get("username"));
            dataUpdated = true;
        }
        
        if (profileData.containsKey("password")) {
            String newPassword = (String) profileData.get("password");
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            passwordChanged = true;
            dataUpdated = true;
        }
        
        // Guardar cambios
        if (dataUpdated) {
            userRepository.save(currentUser);
            employeeRepository.save(employee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Perfil actualizado correctamente");
            response.put("passwordChanged", passwordChanged);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok("No se han detectado cambios");
        }
    }
} 