package com.proyectofinal.frontend.Models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class WorkReport {
    private String id;
    private String employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate reportDate;
    private String startTime;
    private String endTime;
    private Integer breakDuration;
    private String observations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public WorkReport() {}

    public WorkReport(String employeeId, LocalDate reportDate, String startTime, String endTime, 
                     Integer breakDuration, String observations) {
        this.employeeId = employeeId;
        this.reportDate = reportDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakDuration = breakDuration;
        this.observations = observations;
    }

    // Getters y Setters
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

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(Integer breakDuration) {
        this.breakDuration = breakDuration;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Método auxiliar para calcular las horas trabajadas
    public String getWorkedHours() {
        if (startTime != null && endTime != null && breakDuration != null) {
            try {
                String[] startParts = startTime.split(":");
                String[] endParts = endTime.split(":");
                
                int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
                int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
                
                int totalMinutes = endMinutes - startMinutes - breakDuration;
                int hours = totalMinutes / 60;
                int minutes = totalMinutes % 60;
                
                return String.format("%dh %02dm", hours, minutes);
            } catch (Exception e) {
                return "N/A";
            }
        }
        return "N/A";
    }
} 