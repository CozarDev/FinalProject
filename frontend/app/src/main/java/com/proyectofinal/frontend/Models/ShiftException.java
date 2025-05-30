package com.proyectofinal.frontend.Models;

import java.util.Date;

public class ShiftException {
    private String id;
    private String employeeId;   // null significa que aplica a todos los empleados
    private Date startDate;      // Fecha de inicio de la excepción
    private Date endDate;        // Fecha de fin de la excepción
    private String type;         // VACATION, NATIONAL_HOLIDAY, etc.
    private String description;  // Descripción o motivo
    private boolean isAutoGenerated; // Si fue generada automáticamente

    // Variables transitorias (no serializadas)
    private transient Employee employee;

    // Constructor vacío necesario para deserialización
    public ShiftException() {
    }

    // Constructor para festivos nacionales
    public static ShiftException createNationalHoliday(Date date, String description) {
        ShiftException exception = new ShiftException();
        exception.setEmployeeId(null); // Aplica a todos los empleados
        exception.setStartDate(date);
        exception.setEndDate(date);
        exception.setType("NATIONAL_HOLIDAY");
        exception.setDescription(description);
        exception.setAutoGenerated(true);
        return exception;
    }

    // Constructor para vacaciones de empleado
    public static ShiftException createVacation(String employeeId, Date startDate, Date endDate) {
        ShiftException exception = new ShiftException();
        exception.setEmployeeId(employeeId);
        exception.setStartDate(startDate);
        exception.setEndDate(endDate);
        exception.setType("VACATION");
        exception.setDescription("Vacaciones");
        exception.setAutoGenerated(true);
        return exception;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAutoGenerated() {
        return isAutoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        isAutoGenerated = autoGenerated;
    }

    // Getters y setters para propiedades transitorias
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    // Método para verificar si la excepción es global (aplica a todos los empleados)
    public boolean isGlobal() {
        return employeeId == null;
    }

    // Método para verificar si la excepción está activa en una fecha específica
    public boolean isActiveOn(Date date) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        
        return !date.before(startDate) && !date.after(endDate);
    }

    // Método para verificar si es un festivo nacional
    public boolean isNationalHoliday() {
        return "NATIONAL_HOLIDAY".equals(type);
    }

    // Método para verificar si son vacaciones
    public boolean isVacation() {
        return "VACATION".equals(type);
    }
} 