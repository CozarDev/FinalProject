package com.proyectofinal.frontend.Models;

public class Department {
    private String id;
    private String name;
    private String description;
    private String managerId; // Podría ser útil, aunque no lo editemos directamente aquí

    // Constructor vacío (necesario para algunas librerías de deserialización como Gson)
    public Department() {
    }

    // Constructor con parámetros (opcional, pero puede ser útil)
    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters y Setters
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManagerId() {
        return managerId;
    }

    public void setManagerId(String managerId) {
        this.managerId = managerId;
    }
} 