package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    
    public UserService(UserRepository userRepository, EmployeeRepository employeeRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
    }
    
    /**
     * Obtiene el usuario autenticado actualmente
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isCurrentUserAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
    
    /**
     * Verifica si el usuario actual es jefe de departamento
     */
    public boolean isCurrentUserDepartmentHead() {
        User currentUser = getCurrentUser();
        return currentUser != null && "DEPARTMENT_HEAD".equals(currentUser.getRole());
    }
    
    /**
     * Verifica si el usuario actual es el empleado especificado
     */
    public boolean isCurrentUserEmployee(String employeeId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        return employee.isPresent() && employee.get().getUserId().equals(currentUser.getId());
    }
    
    /**
     * Obtiene el ID del empleado asociado al usuario actual
     */
    public String getCurrentEmployeeId() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            Optional<Employee> employee = employeeRepository.findByUserId(currentUser.getId());
            return employee.map(Employee::getId).orElse(null);
        }
        return null;
    }
} 