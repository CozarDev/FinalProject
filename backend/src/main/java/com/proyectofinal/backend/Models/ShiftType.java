package com.proyectofinal.backend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "shift_types")
public class ShiftType {

    @Id
    private String id;
    private String name;
    private String startTime;    // Formato: "HH:MM"
    private String endTime;      // Formato: "HH:MM"
    private boolean[] workDays;  // Array de 7 booleanos [lunes, martes, ..., domingo]
    private String color;        // Color para mostrar en el calendario
    private String createdBy;    // ID del usuario que lo creó

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean[] getWorkDays() {
        return workDays;
    }

    public void setWorkDays(boolean[] workDays) {
        this.workDays = workDays;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
} 