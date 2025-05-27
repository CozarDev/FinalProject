package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.ShiftAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface ShiftAssignmentRepository extends MongoRepository<ShiftAssignment, String> {
    
    // Buscar asignaciones por ID de empleado
    List<ShiftAssignment> findByEmployeeId(String employeeId);
    
    // Buscar asignaciones activas para un empleado en una fecha específica
    @Query("{'employeeId': ?0, 'startDate': {$lte: ?1}, $or: [{'endDate': null}, {'endDate': {$gte: ?1}}]}")
    List<ShiftAssignment> findActiveAssignmentsForEmployeeOnDate(String employeeId, Date date);
    
    // Buscar asignaciones por departamento
    @Query("{'employeeId': {$in: ?0}}")
    List<ShiftAssignment> findByEmployeeIdIn(List<String> employeeIds);
    
    // Buscar asignaciones por fecha de inicio
    List<ShiftAssignment> findByStartDate(LocalDate startDate);
} 