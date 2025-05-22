package com.proyectofinal.backend.Requests;

public class AuthResponse {
    private String token;
    private String role;
    private String userId;
    private String username;

    // Constructor vacío para serialización
    public AuthResponse() {
    }

    // Constructor con todos los campos
    public AuthResponse(String token, String role, String userId, String username) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.username = username;
    }

    // Getters y setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
} 