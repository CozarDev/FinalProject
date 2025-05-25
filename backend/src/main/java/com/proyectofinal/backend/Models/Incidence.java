package com.proyectofinal.backend.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

@Document(collection = "incidences")
public class Incidence {
    
    @Id
    private String id;
    
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    
    // IDs de empleados
    private String createdBy;      // ID del empleado que creó la incidencia
    private String assignedTo;     // ID del empleado de incidencias que la tiene asignada
    
    // Fechas
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Madrid")
    private Date createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Madrid")
    private Date acceptedAt;       // Fecha cuando se aceptó (PENDIENTE → EN_CURSO)
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Europe/Madrid")
    private Date resolvedAt;       // Fecha cuando se resolvió (EN_CURSO → SOLVENTADA)
    
    // Enums para prioridad y estado
    public enum Priority {
        ALTA, MEDIA, BAJA
    }
    
    public enum Status {
        PENDIENTE, EN_CURSO, SOLVENTADA
    }
    
    // Constructores
    public Incidence() {
        this.createdAt = new Date();
        this.status = Status.PENDIENTE;
    }
    
    public Incidence(String title, String description, Priority priority, String createdBy) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.createdBy = createdBy;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getAcceptedAt() {
        return acceptedAt;
    }
    
    public void setAcceptedAt(Date acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    
    public Date getResolvedAt() {
        return resolvedAt;
    }
    
    public void setResolvedAt(Date resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
    
    // Métodos de utilidad
    public boolean canBeAccepted() {
        return status == Status.PENDIENTE;
    }
    
    public boolean canBeResolved() {
        return status == Status.EN_CURSO;
    }
    
    public boolean canBeDeleted() {
        return status == Status.PENDIENTE;
    }
    
    public boolean isActive() {
        return status == Status.PENDIENTE || status == Status.EN_CURSO;
    }
    
    /**
     * Acepta la incidencia (PENDIENTE → EN_CURSO)
     */
    public void accept(String employeeId) {
        if (!canBeAccepted()) {
            throw new IllegalStateException("La incidencia no puede ser aceptada en su estado actual: " + status);
        }
        this.status = Status.EN_CURSO;
        this.assignedTo = employeeId;
        this.acceptedAt = new Date();
    }
    
    /**
     * Resuelve la incidencia (EN_CURSO → SOLVENTADA)
     */
    public void resolve() {
        if (!canBeResolved()) {
            throw new IllegalStateException("La incidencia no puede ser resuelta en su estado actual: " + status);
        }
        this.status = Status.SOLVENTADA;
        this.resolvedAt = new Date();
    }
    
    @Override
    public String toString() {
        return "Incidence{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                ", createdBy='" + createdBy + '\'' +
                ", assignedTo='" + assignedTo + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
} 