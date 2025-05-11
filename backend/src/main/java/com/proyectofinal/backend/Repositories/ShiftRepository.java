package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.Shift;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends MongoRepository<Shift, String> {
    // Métodos personalizados si es necesario
} 