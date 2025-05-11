package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface EmployeeRepository extends MongoRepository<Employee, String> {
    // Métodos personalizados si es necesario
} 