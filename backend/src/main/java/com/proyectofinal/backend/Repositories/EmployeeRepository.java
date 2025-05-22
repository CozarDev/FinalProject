package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
 
@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    // Métodos personalizados si es necesario
    Optional<Employee> findByUserId(String userId);
    
    // Método para buscar empleados por nombre o apellido
    List<Employee> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    
    // Método para buscar empleados por departamento
    List<Employee> findByDepartmentId(String departmentId);
} 