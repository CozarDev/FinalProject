package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Services.WorkReportReminderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/work-report-reminders")
@CrossOrigin(origins = "*")
public class WorkReportReminderController {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkReportReminderController.class);
    
    @Autowired
    private WorkReportReminderService workReportReminderService;
    
    /**
     * Enviar recordatorio manual de parte de trabajo a un empleado específico
     */
    @PostMapping("/send-manual")
    @PreAuthorize("hasRole('DEPARTMENT_HEAD') or hasRole('ADMIN')")
    public ResponseEntity<?> sendManualReminder(@RequestBody Map<String, String> request) {
        try {
            String employeeId = request.get("employeeId");
            String shiftEndTime = request.get("shiftEndTime");
            
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El ID del empleado es obligatorio");
            }
            
            if (shiftEndTime == null || shiftEndTime.trim().isEmpty()) {
                shiftEndTime = "18:00";
            }
            
            workReportReminderService.sendManualWorkReportReminder(employeeId, shiftEndTime);
            
            return ResponseEntity.ok("Recordatorio enviado exitosamente al empleado: " + employeeId);
            
        } catch (Exception e) {
            logger.error("Error enviando recordatorio manual: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error enviando recordatorio: " + e.getMessage());
        }
    }
} 