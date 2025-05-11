package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    // Métodos personalizados si es necesario
} 