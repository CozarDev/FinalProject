package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.Incident;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface IncidentRepository extends MongoRepository<Incident, String> {
    // Métodos personalizados si es necesario
} 