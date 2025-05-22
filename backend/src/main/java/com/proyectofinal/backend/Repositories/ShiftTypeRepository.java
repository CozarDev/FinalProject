package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.ShiftType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftTypeRepository extends MongoRepository<ShiftType, String> {
    // Métodos específicos si se necesitan
} 