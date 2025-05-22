package com.proyectofinal.frontend.Models;

public class ShiftType {
    private String id;
    private String name;
    private String startTime;    // Formato: "HH:MM"
    private String endTime;      // Formato: "HH:MM"
    private boolean[] workDays;  // Array de 7 booleanos [lunes, martes, ..., domingo]
    private String color;        // Color para mostrar en el calendario
    private String createdBy;    // ID del usuario que lo creó

    // Constructor vacío necesario para deserialización
    public ShiftType() {
    }

    // Constructor con parámetros principales
    public ShiftType(String name, String startTime, String endTime, boolean[] workDays, String color) {
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.workDays = workDays;
        this.color = color;
    }

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

    // Método de utilidad para crear una instancia de WorkDays con todos los días laborales
    public static boolean[] createWeekdaySchedule() {
        return new boolean[]{true, true, true, true, true, false, false}; // Lun-Vie
    }

    // Método de utilidad para crear una instancia de WorkDays con todos los días
    public static boolean[] createAllDaysSchedule() {
        return new boolean[]{true, true, true, true, true, true, true}; // Todos los días
    }

    // Método para obtener una representación legible de los días de trabajo
    public String getWorkDaysAsString() {
        if (workDays == null || workDays.length != 7) {
            return "Indefinido";
        }

        StringBuilder sb = new StringBuilder();
        String[] dayNames = {"L", "M", "X", "J", "V", "S", "D"};
        
        for (int i = 0; i < workDays.length; i++) {
            if (workDays[i]) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(dayNames[i]);
            }
        }
        
        return sb.toString();
    }
} 