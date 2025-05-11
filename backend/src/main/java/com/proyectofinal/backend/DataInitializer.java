package com.proyectofinal.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Repositories.UserRepository;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Repositories.EmployeeRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!userRepository.findByUsername("admin").isPresent()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Cambia la contraseña por una más segura
                admin.setRole("ADMIN");
                userRepository.save(admin);

                // Crear empleado genérico
                Employee employee0 = new Employee();
                employee0.setFirstName("Empleado");
                employee0.setLastName("Cero");
                employee0.setEmail("empleado0@empresa.com");
                employee0.setUserId(admin.getId());
                employeeRepository.save(employee0);

                System.out.println("Usuario administrador y empleado genérico creados por defecto.");
            }
        };
    }
} 