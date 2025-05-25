package com.proyectofinal.frontend.Models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonParseException;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.reflect.Type;
import java.util.Locale;

public class Incidence {
    
    private String id;
    private String title;
    private String description;
    private Priority priority;
    private Status status;
    
    // IDs de empleados
    private String createdBy;
    private String assignedTo;
    
    // Fechas
    @JsonAdapter(DateTypeAdapter.class)
    private Date createdAt;
    @JsonAdapter(DateTypeAdapter.class)
    private Date acceptedAt;
    @JsonAdapter(DateTypeAdapter.class)
    private Date resolvedAt;
    
    // Enums para prioridad y estado
    public enum Priority {
        ALTA, MEDIA, BAJA
    }
    
    public enum Status {
        PENDIENTE, EN_CURSO, SOLVENTADA
    }
    
    // Constructores
    public Incidence() {}
    
    public Incidence(String title, String description, Priority priority, String createdBy) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.createdBy = createdBy;
        this.status = Status.PENDIENTE;
        this.createdAt = new Date();
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
    
    // **MÉTODOS DE UTILIDAD**
    
    /**
     * Verifica si la incidencia puede ser aceptada
     */
    public boolean canBeAccepted() {
        return status == Status.PENDIENTE;
    }
    
    /**
     * Verifica si la incidencia puede ser resuelta
     */
    public boolean canBeResolved() {
        return status == Status.EN_CURSO;
    }
    
    /**
     * Verifica si la incidencia puede ser eliminada
     */
    public boolean canBeDeleted() {
        return status == Status.PENDIENTE;
    }
    
    /**
     * Verifica si la incidencia está activa (PENDIENTE o EN_CURSO)
     */
    public boolean isActive() {
        return status == Status.PENDIENTE || status == Status.EN_CURSO;
    }
    
    /**
     * Obtiene el color asociado a la prioridad
     */
    public int getPriorityColor() {
        switch (priority) {
            case ALTA:
                return 0xFFE74C3C; // Rojo
            case MEDIA:
                return 0xFFF39C12; // Naranja
            case BAJA:
                return 0xFF27AE60; // Verde
            default:
                return 0xFF95A5A6; // Gris
        }
    }
    
    /**
     * Obtiene el color asociado al estado
     */
    public int getStatusColor() {
        switch (status) {
            case PENDIENTE:
                return 0xFFF39C12; // Naranja
            case EN_CURSO:
                return 0xFF3498DB; // Azul
            case SOLVENTADA:
                return 0xFF27AE60; // Verde
            default:
                return 0xFF95A5A6; // Gris
        }
    }
    
    /**
     * Obtiene el texto de la prioridad en español
     */
    public String getPriorityText() {
        switch (priority) {
            case ALTA:
                return "Alta";
            case MEDIA:
                return "Media";
            case BAJA:
                return "Baja";
            default:
                return "Desconocida";
        }
    }
    
    /**
     * Obtiene el texto del estado en español
     */
    public String getStatusText() {
        switch (status) {
            case PENDIENTE:
                return "Pendiente";
            case EN_CURSO:
                return "En Curso";
            case SOLVENTADA:
                return "Solventada";
            default:
                return "Desconocido";
        }
    }
    
    /**
     * Obtiene el emoji asociado a la prioridad
     */
    public String getPriorityEmoji() {
        switch (priority) {
            case ALTA:
                return "🔴";
            case MEDIA:
                return "🟡";
            case BAJA:
                return "🟢";
            default:
                return "⚪";
        }
    }
    
    /**
     * Obtiene el emoji asociado al estado
     */
    public String getStatusEmoji() {
        switch (status) {
            case PENDIENTE:
                return "⏳";
            case EN_CURSO:
                return "🔧";
            case SOLVENTADA:
                return "✅";
            default:
                return "❓";
        }
    }
    
    /**
     * Calcula los días transcurridos desde la creación
     */
    public int getDaysOld() {
        if (createdAt == null) {
            return 0;
        }
        
        long diffInMillis = new Date().getTime() - createdAt.getTime();
        return (int) (diffInMillis / (1000 * 60 * 60 * 24));
    }
    
    /**
     * Verifica si la incidencia es urgente (alta prioridad y más de 1 día)
     */
    public boolean isUrgent() {
        return priority == Priority.ALTA && getDaysOld() > 1;
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
    
    // **ADAPTADOR PARA FECHAS**
    public static class DateTypeAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
        private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        
        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : format.format(src));
        }
        
        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.getAsString() == null || json.getAsString().isEmpty()) {
                return null;
            }
            
            try {
                return format.parse(json.getAsString());
            } catch (ParseException e) {
                throw new JsonParseException("Error parsing date: " + json.getAsString(), e);
            }
        }
    }
} 