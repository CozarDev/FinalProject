package com.proyectofinal.frontend.Models;

public class Employee {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String departmentId;
    private String userId;
    
    // Variable transient para mostrar el nombre del departamento (no viene del API)
    private transient String departmentName;

    // Campos adicionales para mostrar en la interfaz
    private transient String username;
    private transient String password;

    // Constructor vacío (necesario para deserialización)
    public Employee() {
    }

    // Constructor con parámetros principales
    public Employee(String firstName, String lastName, String email, String phone, String departmentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.departmentId = departmentId;
    }
    
    // Constructor con parámetros incluyendo username y password
    public Employee(String firstName, String lastName, String email, String phone, String username, String password, String departmentId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.password = password;
        this.departmentId = departmentId;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getDepartmentName() {
        return departmentName;
    }
    
    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    // Método para obtener el nombre completo
    public String getFullName() {
        return firstName + " " + lastName;
    }
} 