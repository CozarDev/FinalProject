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
        // Configura la cadena de filtros de seguridad
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll() // Permite el acceso sin autenticación a las rutas de autenticación
                .requestMatchers("/api/departments").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/departments/manager/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/departments/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                .requestMatchers("/api/employees/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/shifts/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/shifttypes/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .requestMatchers("/api/shiftassignments/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN", "ROLE_DEPARTMENT_HEAD", "DEPARTMENT_HEAD")
                .anyRequest().authenticated() // Requiere autenticación para cualquier otra solicitud
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Configura la política de sesión como sin estado

        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // Añade el filtro de autenticación JWT antes del filtro de autenticación de usuario y contraseña

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Permite todas las procedencias (para desarrollo)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(List.of("x-auth-token"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JWTAuthenticationFilter jwtAuthenticationFilter() {
        // Proporciona una instancia del filtro de autenticación JWT
        return new JWTAuthenticationFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Proporciona un codificador de contraseñas usando BCrypt
        return new BCryptPasswordEncoder();
    }
}