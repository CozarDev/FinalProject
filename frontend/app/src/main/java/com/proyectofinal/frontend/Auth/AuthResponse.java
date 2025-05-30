package com.proyectofinal.frontend.Auth;

public class AuthResponse {
    private String token;
    private String role;
    private String userId;
    private String username;

    // Constructor vacío necesario para deserialización de Gson
    public AuthResponse() {
    }

    // Constructor con todos los parámetros
    public AuthResponse(String token, String role, String userId, String username) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.username = username;
    }

    // Getters y setters


    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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
