// 🔥 SERVICIO CALENDARIFIC COMENTADO - DESCOMENTA SI QUIERES HABILITAR IMPORTACIÓN DE FESTIVOS 🔥
// ================================================================================================
// INSTRUCCIONES PARA HABILITAR CALENDARIFIC:
// 1. Obtén una API key gratuita desde https://calendarific.com/api-documentation
// 2. Configura calendarific.api.key=TU_API_KEY en application.properties
// 3. Descomenta TODO este archivo (quita /* y */)
// 4. Rebuild el proyecto
// ================================================================================================

package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Repositories.ShiftExceptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
import com.proyectofinal.backend.Models.CalendarificModels.Holiday;
import com.proyectofinal.backend.Models.CalendarificModels.HolidayResponse;
import com.proyectofinal.backend.Models.ShiftException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
*/

@Service
public class CalendarificService {

    private final ShiftExceptionRepository shiftExceptionRepository;
    private static final Logger logger = LoggerFactory.getLogger(CalendarificService.class);

    public CalendarificService(ShiftExceptionRepository shiftExceptionRepository) {
        this.shiftExceptionRepository = shiftExceptionRepository;
        logger.info("🚫 Calendarific: DESHABILITADO - Importación automática de festivos no disponible");
        logger.info("💡 Para habilitar: Configura API key en application.properties y descomenta CalendarificService.java");
    }

    /**
     * Método stub que simula la importación de festivos nacionales
     * En la versión deshabilitada, simplemente retorna 0
     */
    public int importNationalHolidays(int year) {
        logger.warn("🚫 Calendarific: Importación de festivos deshabilitada para año {}", year);
        logger.info("💡 Los festivos pueden añadirse manualmente desde la interfaz de administración");
        return 0;
    }
    
    /**
     * Método stub que simula la eliminación de festivos nacionales
     * En la versión deshabilitada, simplemente retorna 0
     */
    public int deleteExistingNationalHolidays(int year) {
        logger.warn("🚫 Calendarific: Eliminación de festivos deshabilitada para año {}", year);
        return 0;
    }
    
    /**
     * Método stub que verifica si existen festivos
     * En la versión deshabilitada, siempre retorna false para permitir operaciones manuales
     */
    public boolean holidaysExistForYear(int year) {
        return false; // Permitir siempre operaciones manuales
    }

    /*
    // CÓDIGO COMENTADO - DESCOMENTA PARA HABILITAR CALENDARIFIC
    // ==========================================================
    
    private final RestTemplate restTemplate;
    
    @Value("${calendarific.api.key}")
    private String apiKey;
    
    private static final String BASE_URL = "https://calendarific.com/api/v2/holidays";
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public CalendarificService(ShiftExceptionRepository shiftExceptionRepository) {
        this.shiftExceptionRepository = shiftExceptionRepository;
        this.restTemplate = createRestTemplateWithSSLDisabled();
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private RestTemplate createRestTemplateWithSSLDisabled() {
        try {
            logger.warn("CREANDO RestTemplate que ignora verificación SSL - ¡SOLO PARA DESARROLLO!");
            
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            return new RestTemplate();
        } catch (Exception e) {
            logger.error("Error configurando RestTemplate sin SSL: {}", e.getMessage());
            logger.warn("Fallback: usando RestTemplate con verificación SSL normal");
            return new RestTemplate();
        }
    }

    public int importNationalHolidays(int year) {
        logger.info("Iniciando importación de festivos nacionales para el año {}", year);
        
        int deletedCount = deleteExistingNationalHolidays(year);
        logger.info("Eliminados {} festivos existentes para el año {}", deletedCount, year);
        
        String url = BASE_URL + "?api_key=" + apiKey + "&country=ES&year=" + year;
        logger.info("Consultando API de Calendarific para año {}", year);
        
        try {
            logger.info("Llamando a la API de Calendarific...");
            HolidayResponse response = restTemplate.getForObject(url, HolidayResponse.class);
            
            if (response == null) {
                logger.error("La respuesta de la API es null");
                return 0;
            }
            
            if (response.getResponse() == null) {
                logger.error("El campo 'response' de la API es null");
                return 0;
            }
            
            if (response.getResponse().getHolidays() == null) {
                logger.error("El campo 'holidays' de la respuesta es null");
                return 0;
            }
            
            List<Holiday> holidays = response.getResponse().getHolidays();
            logger.info("API devolvió {} festivos totales para España {}", holidays.size(), year);
            
            int count = 0;
            
            for (Holiday holiday : holidays) {
                if (holiday.getType() != null && holiday.getType().contains("National holiday")) {
                    try {
                        ShiftException exception = convertHolidayToException(holiday);
                        shiftExceptionRepository.save(exception);
                        count++;
                    } catch (Exception e) {
                        logger.error("Error al convertir/guardar festivo {}: {}", holiday.getName(), e.getMessage());
                    }
                }
            }
            
            logger.info("Importación completada: {} festivos nacionales guardados para el año {}", count, year);
            return count;
        } catch (Exception e) {
            logger.error("Error al importar festivos nacionales para el año {}: {}", year, e.getMessage(), e);
            return 0;
        }
    }
    
    public int deleteExistingNationalHolidays(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        Date yearStart = cal.getTime();
        
        cal.set(year + 1, Calendar.JANUARY, 1, 0, 0, 0);
        Date yearEnd = cal.getTime();
        
        List<ShiftException> holidays = shiftExceptionRepository.findByTypeAndEmployeeIdIsNullAndDateRange(
                "NATIONAL_HOLIDAY", yearStart, yearEnd);
        
        int count = holidays.size();
        shiftExceptionRepository.deleteAll(holidays);
        
        return count;
    }
    
    public boolean holidaysExistForYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        Date yearStart = cal.getTime();
        
        cal.set(year + 1, Calendar.JANUARY, 1, 0, 0, 0);
        Date yearEnd = cal.getTime();
        
        List<ShiftException> holidays = shiftExceptionRepository.findByTypeAndYear("NATIONAL_HOLIDAY", yearStart, yearEnd);
        return !holidays.isEmpty();
    }
    
    private ShiftException convertHolidayToException(Holiday holiday) throws ParseException {
        if (holiday == null) {
            throw new IllegalArgumentException("Holiday no puede ser null");
        }
        
        if (holiday.getDate() == null || holiday.getDate().getIso() == null) {
            throw new IllegalArgumentException("Fecha del festivo no válida: " + holiday.getName());
        }
        
        ShiftException exception = new ShiftException();
        exception.setEmployeeId(null);
        exception.setType("NATIONAL_HOLIDAY");
        exception.setDescription(holiday.getName());
        exception.setAutoGenerated(true);
        
        String isoDate = holiday.getDate().getIso();
        try {
            Date holidayDate = ISO_FORMAT.parse(isoDate);
            
            exception.setStartDate(holidayDate);
            exception.setEndDate(holidayDate);
            
            return exception;
        } catch (ParseException e) {
            logger.error("Error al parsear fecha ISO '{}' para festivo '{}'", isoDate, holiday.getName());
            throw e;
        }
    }
    */
} 