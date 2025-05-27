package com.proyectofinal.backend.Requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;

public class WorkReportRequest {
    
    @NotNull(message = "La fecha del parte es obligatoria")
    private LocalDate reportDate;
    
    @NotBlank(message = "La hora de inicio es obligatoria")
    private String startTime;
    
    @NotBlank(message = "La hora de fin es obligatoria")
    private String endTime;
    
    @NotNull(message = "La duración del descanso es obligatoria")
    @Min(value = 0, message = "La duración del descanso no puede ser negativa")
    @Max(value = 480, message = "La duración del descanso no puede ser mayor a 8 horas")
    private Integer breakDuration;
    
    private String observations;

    // Constructores
    public WorkReportRequest() {}

    public WorkReportRequest(LocalDate reportDate, String startTime, String endTime, 
                           Integer breakDuration, String observations) {
        this.reportDate = reportDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakDuration = breakDuration;
        this.observations = observations;
    }

    // Getters y Setters
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
} 