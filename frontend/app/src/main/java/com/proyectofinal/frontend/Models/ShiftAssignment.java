package com.proyectofinal.frontend.Models;

import java.util.Date;

public class ShiftAssignment {
    private String id;
    private String employeeId;
    private String shiftTypeId;
    private Date startDate;
    private Date endDate;      // null significa indefinido
    private String assignedBy; // ID del usuario que asignó
    private Date assignedAt;   // Fecha de asignación

    // Variables transitorias (no serializadas)
    private transient Employee employee;
    private transient ShiftType shiftType;

    // Constructor vacío necesario para deserialización
    public ShiftAssignment() {
    }

    // Constructor con parámetros principales
    public ShiftAssignment(String employeeId, String shiftTypeId, Date startDate, Date endDate) {
        this.employeeId = employeeId;
        this.shiftTypeId = shiftTypeId;
        this.startDate = startDate;
        this.endDate = endDate;
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

    public String getShiftTypeId() {
        return shiftTypeId;
    }

    public void setShiftTypeId(String shiftTypeId) {
        this.shiftTypeId = shiftTypeId;
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

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public Date getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
    }

    // Getters y setters para propiedades transitorias
    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public ShiftType getShiftType() {
        return shiftType;
    }

    public void setShiftType(ShiftType shiftType) {
        this.shiftType = shiftType;
    }

    // Método para verificar si la asignación es indefinida
    public boolean isIndefinite() {
        return endDate == null;
    }

    // Método para verificar si la asignación está activa en una fecha específica
    public boolean isActiveOn(Date date) {
        if (date == null || startDate == null) {
            return false;
        }
        
        boolean afterStart = !date.before(startDate);
        boolean beforeEnd = endDate == null || !date.after(endDate);
        
        return afterStart && beforeEnd;
    }
} 