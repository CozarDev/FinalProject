package com.proyectofinal.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.proyectofinal.backend.Security.JWTAuthenticationFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/departments").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/departments/manager/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/departments/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                .requestMatchers("/api/employees/me").authenticated()
                .requestMatchers("/api/employees/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/shifts/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/shifttypes/**").authenticated() // Necesario para calcular turnos
                .requestMatchers("/api/shiftassignments/**").authenticated() // Controlador maneja permisos específicos
                .requestMatchers("/api/holidays/**").authenticated() // Controlador maneja permisos específicos
                .requestMatchers("/api/incidences/**").authenticated() // Controlador maneja permisos específicos
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000", "http://10.0.2.2:8080"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        return new JWTAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}