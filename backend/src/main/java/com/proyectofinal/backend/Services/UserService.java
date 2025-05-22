package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.Department;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.UserRepository;
import com.proyectofinal.backend.Repositories.DepartmentRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    
    public UserService(
            UserRepository userRepository, 
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
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
    
    /**
     * Verifica si el usuario actual es jefe del departamento especificado
     */
    public boolean isCurrentUserDepartmentHeadOf(String departmentId) {
        // Si el usuario es admin, tiene todos los permisos
        if (isCurrentUserAdmin()) {
            return true;
        }
        
        // Si no es jefe de departamento, no tiene permisos
        if (!isCurrentUserDepartmentHead()) {
            return false;
        }
        
        // Obtener el empleado asociado al usuario actual
        String currentEmployeeId = getCurrentEmployeeId();
        if (currentEmployeeId == null) {
            return false;
        }
        
        // Verificar si el empleado es jefe del departamento especificado
        Optional<Department> department = departmentRepository.findById(departmentId);
        return department.isPresent() && currentEmployeeId.equals(department.get().getManagerId());
    }
    
    /**
     * Verifica si el usuario actual es jefe del departamento especificado
     * (Alias para isCurrentUserDepartmentHeadOf)
     */
    public boolean isCurrentUserManagerOfDepartment(String departmentId) {
        return isCurrentUserDepartmentHeadOf(departmentId);
    }
    
    /**
     * Verifica si el usuario actual es jefe del departamento del empleado especificado
     */
    public boolean isCurrentUserDepartmentHeadOfEmployee(String employeeId) {
        // Si el usuario es admin, tiene todos los permisos
        if (isCurrentUserAdmin()) {
            return true;
        }
        
        // Si no es jefe de departamento, no tiene permisos
        if (!isCurrentUserDepartmentHead()) {
            return false;
        }
        
        // Obtener el empleado especificado
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (!employee.isPresent()) {
            return false;
        }
        
        // Obtener el departamento del empleado
        String departmentId = employee.get().getDepartmentId();
        if (departmentId == null) {
            return false;
        }
        
        // Verificar si el usuario actual es jefe de ese departamento
        return isCurrentUserDepartmentHeadOf(departmentId);
    }
} 