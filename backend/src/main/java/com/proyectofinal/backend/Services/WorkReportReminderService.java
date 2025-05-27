package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.ShiftAssignment;
import com.proyectofinal.backend.Models.WorkReport;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.ShiftAssignmentRepository;
import com.proyectofinal.backend.Repositories.WorkReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class WorkReportReminderService {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkReportReminderService.class);
    
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;
    
    @Autowired
    private WorkReportRepository workReportRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;
    
    /**
     * Ejecuta cada 5 minutos para verificar si hay empleados que terminaron su turno
     * hace más de 5 minutos y no han subido su parte de trabajo
     */
    @Scheduled(fixedRate = 300000) // 5 minutos = 300,000 ms
    public void checkWorkReportReminders() {
        try {
            logger.debug("Verificando recordatorios de partes de trabajo...");
            
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();
            
            // Buscar asignaciones de turno que terminaron hoy
            List<ShiftAssignment> todayAssignments = shiftAssignmentRepository.findByStartDate(today);
            
            for (ShiftAssignment assignment : todayAssignments) {
                try {
                    // Verificar si el turno ya terminó hace más de 5 minutos
                    if (assignment.getEndDate() != null && assignment.getEndDate().isBefore(today)) {
                        // El turno terminó en una fecha anterior, no procesar
                        continue;
                    }
                    
                    // Calcular la hora de fin del turno (asumiendo que es hoy)
                    LocalDateTime shiftEndTime = calculateShiftEndTime(assignment, today);
                    
                    if (shiftEndTime != null && shiftEndTime.isBefore(now.minusMinutes(5))) {
                        // El turno terminó hace más de 5 minutos
                        
                        // Verificar si ya existe un parte de trabajo para este empleado y fecha
                        boolean hasWorkReport = workReportRepository.existsByEmployeeIdAndReportDate(
                            assignment.getEmployeeId(), 
                            today
                        );
                        
                        if (!hasWorkReport) {
                            // No hay parte de trabajo, enviar recordatorio
                            sendWorkReportReminder(assignment, shiftEndTime);
                        }
                    }
                    
                } catch (Exception e) {
                    logger.error("Error procesando asignación {}: {}", assignment.getId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error en verificación de recordatorios de partes de trabajo: {}", e.getMessage());
        }
    }
    
    /**
     * Calcula la hora de fin del turno basándose en la asignación
     */
    private LocalDateTime calculateShiftEndTime(ShiftAssignment assignment, LocalDate date) {
        try {
            // Aquí deberías implementar la lógica específica de tu aplicación
            // para calcular cuándo termina un turno basándose en el tipo de turno
            
            // Por ahora, asumimos que los turnos terminan a las 18:00
            // Puedes modificar esto según tu lógica de negocio
            
            // Si tienes información del tipo de turno, podrías usar:
            // ShiftType shiftType = shiftTypeRepository.findById(assignment.getShiftTypeId());
            // y calcular la hora de fin basándose en la duración del turno
            
            return LocalDateTime.of(date, LocalTime.of(18, 0)); // 18:00 por defecto
            
        } catch (Exception e) {
            logger.error("Error calculando hora de fin del turno para asignación {}: {}", 
                        assignment.getId(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Envía recordatorio de parte de trabajo a un empleado
     */
    private void sendWorkReportReminder(ShiftAssignment assignment, LocalDateTime shiftEndTime) {
        try {
            // Obtener información del empleado
            Optional<Employee> employeeOpt = employeeRepository.findById(assignment.getEmployeeId());
            
            if (employeeOpt.isEmpty()) {
                logger.warn("No se encontró empleado con ID: {}", assignment.getEmployeeId());
                return;
            }
            
            Employee employee = employeeOpt.get();
            String employeeUserId = employee.getUserId();
            
            if (employeeUserId == null || employeeUserId.trim().isEmpty()) {
                logger.warn("Empleado {} no tiene userId para notificaciones", assignment.getEmployeeId());
                return;
            }
            
            // Formatear hora de fin del turno
            String shiftEndTimeStr = shiftEndTime.toLocalTime().toString();
            
            logger.info("Enviando recordatorio de parte de trabajo al empleado: {} (turno terminó a las {})", 
                       employeeUserId, shiftEndTimeStr);
            
            // Enviar notificación
            firebaseMessagingService.sendWorkReportReminder(employeeUserId, shiftEndTimeStr);
            
            logger.info("Recordatorio de parte de trabajo enviado al empleado: {}", employeeUserId);
            
        } catch (Exception e) {
            logger.error("Error enviando recordatorio de parte de trabajo para asignación {}: {}", 
                        assignment.getId(), e.getMessage());
        }
    }
    
    /**
     * Método manual para enviar recordatorio a un empleado específico
     * (puede ser llamado desde un endpoint si es necesario)
     */
    public void sendManualWorkReportReminder(String employeeId, String shiftEndTime) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            
            if (employeeOpt.isEmpty()) {
                throw new RuntimeException("Empleado no encontrado: " + employeeId);
            }
            
            Employee employee = employeeOpt.get();
            String employeeUserId = employee.getUserId();
            
            if (employeeUserId == null || employeeUserId.trim().isEmpty()) {
                throw new RuntimeException("Empleado no tiene userId para notificaciones: " + employeeId);
            }
            
            logger.info("Enviando recordatorio manual de parte de trabajo al empleado: {}", employeeUserId);
            
            firebaseMessagingService.sendWorkReportReminder(employeeUserId, shiftEndTime);
            
            logger.info("Recordatorio manual enviado al empleado: {}", employeeUserId);
            
        } catch (Exception e) {
            logger.error("Error enviando recordatorio manual: {}", e.getMessage());
            throw new RuntimeException("Error enviando recordatorio: " + e.getMessage());
        }
    }
} 