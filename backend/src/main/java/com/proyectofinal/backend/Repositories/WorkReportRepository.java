package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.WorkReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkReportRepository extends MongoRepository<WorkReport, String> {
    
    // Obtener todos los partes ordenados por fecha de creación (más recientes primero)
    List<WorkReport> findAllByOrderByCreatedAtDesc();
    
    // Obtener partes de un empleado específico ordenados por fecha de creación
    List<WorkReport> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);
    
    // Verificar si un empleado ya tiene un parte para una fecha específica
    boolean existsByEmployeeIdAndReportDate(String employeeId, LocalDate reportDate);
    
    // Obtener partes por lista de empleados (para departamentos)
    List<WorkReport> findByEmployeeIdInOrderByCreatedAtDesc(List<String> employeeIds);
} 