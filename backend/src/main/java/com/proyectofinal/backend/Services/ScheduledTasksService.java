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
    
    private final CalendarificService calendarificService;
    private final VacationService vacationService;
    
    public ScheduledTasksService(
            CalendarificService calendarificService,
            VacationService vacationService) {
        this.calendarificService = calendarificService;
        this.vacationService = vacationService;
    }
    
    /**
     * Tarea programada para ejecutarse el 31 de diciembre a las 23:59 cada año.
     * Importa los festivos nacionales y genera las vacaciones automáticas para el año siguiente.
     */
    @Scheduled(cron = "0 59 23 31 12 *")
    public void yearEndProcessing() {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        int nextYear = now.getYear() + 1;
        
        logger.info("Iniciando procesamiento de fin de año: " + now);
        logger.info("Importando festivos nacionales para el año " + nextYear);
        
        try {
            // Importar festivos nacionales para el próximo año
            int holidaysImported = calendarificService.importNationalHolidays(nextYear);
            logger.info("Se importaron " + holidaysImported + " festivos nacionales para el año " + nextYear);
            
            // Generar vacaciones automáticas para el próximo año
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
            // Importar festivos nacionales
            int holidaysImported = calendarificService.importNationalHolidays(year);
            
            // Generar vacaciones automáticas
            int vacationsAssigned = vacationService.assignVacationsForYear(year);
            
            String result = "Procesamiento completado para el año " + year + ":\n" +
                    "- Festivos nacionales importados: " + holidaysImported + "\n" +
                    "- Empleados con vacaciones asignadas: " + vacationsAssigned;
            
            logger.info(result);
            return result;
        } catch (Exception e) {
            String errorMsg = "Error durante el procesamiento para el año " + year + ": " + e.getMessage();
            logger.error(errorMsg, e);
            return errorMsg;
        }
    }
} 