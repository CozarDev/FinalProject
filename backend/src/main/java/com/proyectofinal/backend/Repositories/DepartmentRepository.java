package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
 
@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    // Métodos personalizados si es necesario
    // Método para encontrar departamentos por ID del manager
    List<Department> findByManagerId(String managerId);
    
    // Método para encontrar departamento por nombre (insensible a mayúsculas/minúsculas)
    Department findByNameIgnoreCase(String name);
} 