package com.proyectofinal.backend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "shiftAssignments")
public class ShiftAssignment {
    
    @Id
    private String id;
    
    private String employeeId;
    private String shiftTypeId;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Constructor vacío requerido por Spring Data
    public ShiftAssignment() {
    }
    
    // Constructor completo
    public ShiftAssignment(String id, String employeeId, String shiftTypeId, LocalDate startDate, LocalDate endDate) {
        this.id = id;
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
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    @Override
    public String toString() {
        return "ShiftAssignment{" +
                "id='" + id + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", shiftTypeId='" + shiftTypeId + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
} 