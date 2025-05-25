package com.proyectofinal.backend.Repositories;

import com.proyectofinal.backend.Models.Incidence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenceRepository extends MongoRepository<Incidence, String> {
    
    // **CONSULTAS POR ESTADO**
    
    /**
     * Obtener incidencias pendientes ordenadas por prioridad (ALTA → MEDIA → BAJA)
     */
    List<Incidence> findByStatusOrderByPriorityAsc(Incidence.Status status);
    
    /**
     * Obtener incidencias activas (PENDIENTE o EN_CURSO) ordenadas por prioridad
     */
    @Query("{ 'status': { $in: ['PENDIENTE', 'EN_CURSO'] } }")
    List<Incidence> findActiveIncidencesOrderByPriority();
    
    /**
     * Obtener incidencias pendientes específicamente
     */
    List<Incidence> findByStatusEquals(Incidence.Status status);
    
    // **CONSULTAS POR EMPLEADO CREADOR**
    
    /**
     * Obtener todas las incidencias creadas por un empleado específico
     */
    List<Incidence> findByCreatedByOrderByCreatedAtDesc(String employeeId);
    
    /**
     * Obtener incidencias creadas por un empleado con estado específico
     */
    List<Incidence> findByCreatedByAndStatusOrderByCreatedAtDesc(String employeeId, Incidence.Status status);
    
    /**
     * Obtener incidencias pendientes creadas por un empleado (para poder borrarlas)
     */
    List<Incidence> findByCreatedByAndStatus(String employeeId, Incidence.Status status);
    
    // **CONSULTAS POR EMPLEADO ASIGNADO**
    
    /**
     * Obtener incidencias asignadas a un empleado de incidencias
     */
    List<Incidence> findByAssignedToOrderByCreatedAtDesc(String employeeId);
    
    /**
     * Obtener incidencias en curso asignadas a un empleado específico
     */
    List<Incidence> findByAssignedToAndStatus(String employeeId, Incidence.Status status);
    
    // **CONSULTAS POR DEPARTAMENTO**
    
    /**
     * Obtener incidencias creadas por empleados de un departamento específico
     * (requiere join con Employee)
     */
    @Query("{ 'createdBy': { $in: ?0 } }")
    List<Incidence> findByCreatedByInOrderByCreatedAtDesc(List<String> employeeIds);
    
    /**
     * Obtener incidencias pendientes creadas por empleados de un departamento
     */
    @Query("{ 'createdBy': { $in: ?0 }, 'status': ?1 }")
    List<Incidence> findByCreatedByInAndStatus(List<String> employeeIds, Incidence.Status status);
    
    // **CONSULTAS ESTADÍSTICAS**
    
    /**
     * Contar incidencias por estado
     */
    long countByStatus(Incidence.Status status);
    
    /**
     * Contar incidencias creadas por un empleado
     */
    long countByCreatedBy(String employeeId);
    
    /**
     * Contar incidencias asignadas a un empleado
     */
    long countByAssignedTo(String employeeId);
    
    // **CONSULTAS PERSONALIZADAS CON MONGO QUERY**
    
    /**
     * Obtener incidencias activas ordenadas por prioridad y fecha de creación
     */
    @Query(value = "{ 'status': { $in: ['PENDIENTE', 'EN_CURSO'] } }", 
           sort = "{ 'priority': 1, 'createdAt': -1 }")
    List<Incidence> findActiveIncidencesOrderByPriorityAndDate();
    
    /**
     * Obtener todas las incidencias ordenadas por prioridad y fecha
     */
    @Query(sort = "{ 'priority': 1, 'createdAt': -1 }")
    List<Incidence> findAllOrderByPriorityAndCreatedAt();
    
    /**
     * Buscar incidencias por título o descripción (búsqueda de texto)
     */
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Incidence> findByTitleOrDescriptionContainingIgnoreCase(String searchText);
} 