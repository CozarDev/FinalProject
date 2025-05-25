package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.CalendarificModels.Holiday;
import com.proyectofinal.backend.Models.CalendarificModels.HolidayResponse;
import com.proyectofinal.backend.Models.ShiftException;
import com.proyectofinal.backend.Repositories.ShiftExceptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class CalendarificService {

    private final ShiftExceptionRepository shiftExceptionRepository;
    private final RestTemplate restTemplate;
    
    @Value("${calendarific.api.key}")
    private String apiKey;
    
    private static final String BASE_URL = "https://calendarific.com/api/v2/holidays";
    private static final SimpleDateFormat ISO_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final Logger logger = LoggerFactory.getLogger(CalendarificService.class);

    public CalendarificService(ShiftExceptionRepository shiftExceptionRepository) {
        this.shiftExceptionRepository = shiftExceptionRepository;
        this.restTemplate = createRestTemplateWithSSLDisabled();
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Crea un RestTemplate que ignora la verificación SSL
     * ¡SOLO PARA DESARROLLO! En producción debería usarse un certificado válido
     */
    private RestTemplate createRestTemplateWithSSLDisabled() {
        try {
            logger.warn("CREANDO RestTemplate que ignora verificación SSL - ¡SOLO PARA DESARROLLO!");
            
            // Crear un TrustManager que confía en todos los certificados
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

            // Crear un SSLContext que usa nuestro TrustManager que confía en todo
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Configurar HttpsURLConnection para usar nuestro SSLContext
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            
            // Crear un HostnameVerifier que acepta todos los hostnames
            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            return new RestTemplate();
        } catch (Exception e) {
            logger.error("Error configurando RestTemplate sin SSL: {}", e.getMessage());
            logger.warn("Fallback: usando RestTemplate con verificación SSL normal");
            return new RestTemplate();
        }
    }

    /**
     * Importa los festivos nacionales de España para un año específico.
     * Primero elimina los festivos existentes del mismo año para evitar duplicados.
     * @param year El año para el que se importarán los festivos
     * @return Número de festivos importados
     */
    public int importNationalHolidays(int year) {
        logger.info("Iniciando importación de festivos nacionales para el año {}", year);
        
        // Primero eliminamos los festivos nacionales existentes para este año
        int deletedCount = deleteExistingNationalHolidays(year);
        logger.info("Eliminados {} festivos existentes para el año {}", deletedCount, year);
        
        String url = BASE_URL + "?api_key=" + apiKey + "&country=ES&year=" + year;
        logger.debug("URL de la API: {}", url.replace(apiKey, "***"));
        
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
                logger.debug("Procesando festivo: {} - Tipos: {}", holiday.getName(), holiday.getType());
                
                // Solo importar festivos nacionales
                if (holiday.getType() != null && holiday.getType().contains("National holiday")) {
                    try {
                        ShiftException exception = convertHolidayToException(holiday);
                        shiftExceptionRepository.save(exception);
                        count++;
                        logger.debug("Festivo guardado: {} en fecha {}", holiday.getName(), holiday.getDate().getIso());
                    } catch (Exception e) {
                        logger.error("Error al convertir/guardar festivo {}: {}", holiday.getName(), e.getMessage());
                    }
                } else {
                    logger.debug("Saltando festivo '{}' - no es nacional", holiday.getName());
                }
            }
            
            logger.info("Importación completada: {} festivos nacionales guardados para el año {}", count, year);
            return count;
        } catch (Exception e) {
            logger.error("Error al importar festivos nacionales para el año {}: {}", year, e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * Elimina los festivos nacionales existentes para un año específico
     * @param year El año para el que se eliminarán los festivos
     * @return Número de festivos eliminados
     */
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
    
    /**
     * Verifica si ya existen festivos importados para un año específico
     * @param year El año para verificar
     * @return true si ya existen festivos para ese año
     */
    public boolean holidaysExistForYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        Date yearStart = cal.getTime();
        
        cal.set(year + 1, Calendar.JANUARY, 1, 0, 0, 0);
        Date yearEnd = cal.getTime();
        
        List<ShiftException> holidays = shiftExceptionRepository.findByTypeAndYear("NATIONAL_HOLIDAY", yearStart, yearEnd);
        return !holidays.isEmpty();
    }
    
    /**
     * Convierte un objeto Holiday de Calendarific a un ShiftException
     */
    private ShiftException convertHolidayToException(Holiday holiday) throws ParseException {
        if (holiday == null) {
            throw new IllegalArgumentException("Holiday no puede ser null");
        }
        
        if (holiday.getDate() == null || holiday.getDate().getIso() == null) {
            throw new IllegalArgumentException("Fecha del festivo no válida: " + holiday.getName());
        }
        
        logger.debug("Convirtiendo festivo: {} - Fecha ISO: {}", holiday.getName(), holiday.getDate().getIso());
        
        ShiftException exception = new ShiftException();
        exception.setEmployeeId(null); // Aplica a todos los empleados
        exception.setType("NATIONAL_HOLIDAY");
        exception.setDescription(holiday.getName());
        exception.setAutoGenerated(true);
        
        // Parsear la fecha ISO
        String isoDate = holiday.getDate().getIso();
        try {
            Date holidayDate = ISO_FORMAT.parse(isoDate);
            
            exception.setStartDate(holidayDate);
            exception.setEndDate(holidayDate); // Mismo día inicio y fin para festivos de un día
            
            logger.debug("Festivo convertido exitosamente: {} -> {}", holiday.getName(), holidayDate);
            return exception;
        } catch (ParseException e) {
            logger.error("Error al parsear fecha ISO '{}' para festivo '{}'", isoDate, holiday.getName());
            throw e;
        }
    }
} 