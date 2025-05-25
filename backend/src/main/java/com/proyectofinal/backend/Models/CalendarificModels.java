package com.proyectofinal.backend.Models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// Clases para deserializar la respuesta de Calendarific API
public class CalendarificModels {

    // Clase para la respuesta completa
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HolidayResponse {
        private Meta meta;
        private Response response;

        public Meta getMeta() { return meta; }
        public void setMeta(Meta meta) { this.meta = meta; }

        public Response getResponse() { return response; }
        public void setResponse(Response response) { this.response = response; }
    }

    // Clase para el meta de la respuesta
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private int code;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
    }

    // Clase para el cuerpo de la respuesta
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private List<Holiday> holidays;

        public List<Holiday> getHolidays() { return holidays; }
        public void setHolidays(List<Holiday> holidays) { this.holidays = holidays; }
    }

    // Clase para cada festivo - SOLO los campos esenciales
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Holiday {
        private String name;
        private String description;
        private Country country;
        private HolidayDate date;
        private List<String> type;

        // Solo getters y setters para los campos esenciales
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Country getCountry() { return country; }
        public void setCountry(Country country) { this.country = country; }

        public HolidayDate getDate() { return date; }
        public void setDate(HolidayDate date) { this.date = date; }

        public List<String> getType() { return type; }
        public void setType(List<String> type) { this.type = type; }
    }

    // Clase para la fecha del festivo
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HolidayDate {
        private String iso;
        private HolidayDateTime datetime;

        public String getIso() { return iso; }
        public void setIso(String iso) { this.iso = iso; }

        public HolidayDateTime getDatetime() { return datetime; }
        public void setDatetime(HolidayDateTime datetime) { this.datetime = datetime; }
    }

    // Clase para la fecha y hora del festivo
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HolidayDateTime {
        private int year;
        private int month;
        private int day;

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public int getMonth() { return month; }
        public void setMonth(int month) { this.month = month; }

        public int getDay() { return day; }
        public void setDay(int day) { this.day = day; }
    }

    // Clase para el objeto country
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Country {
        private String id;
        private String name;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
} 