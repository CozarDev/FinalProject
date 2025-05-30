package com.proyectofinal.backend.Services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;

@Service
public class ScheduledTasksService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasksService.class);
    
    // 🔥 CALENDARIFIC COMENTADO - DESCOMENTA SI QUIERES HABILITAR IMPORTACIÓN AUTOMÁTICA DE FESTIVOS 🔥
    // private final CalendarificService calendarificService;
    private final VacationService vacationService;
    
    public ScheduledTasksService(
            // CalendarificService calendarificService,  // 🔥 COMENTADO - Calendarific deshabilitado
            VacationService vacationService) {
        // this.calendarificService = calendarificService;  // 🔥 COMENTADO - Calendarific deshabilitado
        this.vacationService = vacationService;
        
        logger.info("🚫 ScheduledTasksService: Calendarific deshabilitado - Importación automática de festivos no disponible");
        logger.info("💡 Las vacaciones automáticas seguirán funcionando normalmente");
    }
    
    /**
     * Tarea programada para ejecutarse el 31 de diciembre a las 23:59 cada año.
     * Genera las vacaciones automáticas para el año siguiente.
     * NOTA: Importación de festivos está deshabilitada (requiere Calendarific API)
     */
    @Scheduled(cron = "0 59 23 31 12 *")
    public void yearEndProcessing() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        int nextYear = now.getYear() + 1;
        
        logger.info("Iniciando procesamiento de fin de año: " + now);
        
        try {
            // 🔥 COMENTADO - Calendarific deshabilitado
            // logger.info("Importando festivos nacionales para el año " + nextYear);
            // int holidaysImported = calendarificService.importNationalHolidays(nextYear);
            // logger.info("Se importaron " + holidaysImported + " festivos nacionales para el año " + nextYear);
            
            logger.info("🚫 Importación de festivos omitida - Calendarific deshabilitado");
            logger.info("💡 Los festivos pueden añadirse manualmente desde la interfaz de administración");
            
            // Generar vacaciones automáticas para el próximo año
            logger.info("Generando vacaciones automáticas para el año " + nextYear);
            int vacationsAssigned = vacationService.assignVacationsForYear(nextYear);
            logger.info("Se asignaron vacaciones a " + vacationsAssigned + " empleados para el año " + nextYear);
            
            logger.info("Procesamiento de fin de año completado con éxito");
        } catch (Exception e) {
            logger.error("Error durante el procesamiento de fin de año", e);
        }
    }
    
    /**
     * Método para forzar la ejecución del procesamiento de fin de año desde un endpoint
     * @param year El año para el que se realizará el procesamiento
     * @return Resumen de las operaciones realizadas
     */
    public String forceYearEndProcessing(int year) {
        logger.info("Forzando procesamiento para el año " + year);
        
        try {
            // 🔥 COMENTADO - Calendarific deshabilitado
            // Importar festivos nacionales
            // int holidaysImported = calendarificService.importNationalHolidays(year);
            int holidaysImported = 0; // Valor por defecto cuando Calendarific está deshabilitado
            
            // Generar vacaciones automáticas
            int vacationsAssigned = vacationService.assignVacationsForYear(year);
            
            String result = "Procesamiento completado para el año " + year + ":\n" +
                    "- Festivos nacionales importados: " + holidaysImported + " (🚫 Calendarific deshabilitado)\n" +
                    "- Empleados con vacaciones asignadas: " + vacationsAssigned + "\n" +
                    "💡 Los festivos pueden añadirse manualmente desde la interfaz de administración";
            
            logger.info(result);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error durante el procesamiento para el año " + year + ": " + e.getMessage();
            logger.error(errorMsg, e);
            return errorMsg;
        }
    }
} 