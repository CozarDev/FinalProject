package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.ShiftException;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface ShiftExceptionRepository extends MongoRepository<ShiftException, String> {
    
    // Buscar excepciones por ID de empleado
    List<ShiftException> findByEmployeeId(String employeeId);
    
    // Buscar excepciones globales (aplicables a todos los empleados)
    List<ShiftException> findByEmployeeIdIsNull();
    
    // Buscar excepciones activas para un empleado en una fecha específica
    @Query("{'employeeId': ?0, 'startDate': {$lte: ?1}, 'endDate': {$gte: ?1}}")
    List<ShiftException> findActiveExceptionsForEmployeeOnDate(String employeeId, Date date);
    
    // Buscar excepciones globales activas en una fecha específica
    @Query("{'employeeId': null, 'startDate': {$lte: ?0}, 'endDate': {$gte: ?0}}")
    List<ShiftException> findActiveGlobalExceptionsOnDate(Date date);
    
    // Buscar excepciones por tipo
    List<ShiftException> findByType(String type);
    
    // Buscar excepciones por tipo y año
    @Query("{'type': ?0, 'startDate': {$gte: ?1}, 'startDate': {$lt: ?2}}")
    List<ShiftException> findByTypeAndYear(String type, Date yearStart, Date yearEnd);
    
    // Buscar excepciones por ID de empleado, tipo y rango de fechas
    @Query("{'employeeId': ?0, 'type': ?1, 'startDate': {$gte: ?2}, 'startDate': {$lte: ?3}}")
    List<ShiftException> findByEmployeeIdAndTypeAndDateRange(String employeeId, String type, Date startDate, Date endDate);
    
    // Buscar excepciones por ID de empleado y tipo
    List<ShiftException> findByEmployeeIdAndType(String employeeId, String type);
    
    // Buscar excepciones por tipo y empleado nulo
    List<ShiftException> findByTypeAndEmployeeIdIsNull(String type);
    
    // Buscar excepciones por tipo, empleado nulo y rango de fechas
    @Query("{'type': ?0, 'employeeId': null, 'startDate': {$gte: ?1}, 'startDate': {$lt: ?2}}")
    List<ShiftException> findByTypeAndEmployeeIdIsNullAndDateRange(String type, Date startDate, Date endDate);
    
    // Buscar excepciones por tipo y rango de fechas (independiente del empleado)
    @Query("{'type': ?0, 'startDate': {$gte: ?1}, 'startDate': {$lte: ?2}}")
    List<ShiftException> findByTypeAndDateRange(String type, Date startDate, Date endDate);
} 