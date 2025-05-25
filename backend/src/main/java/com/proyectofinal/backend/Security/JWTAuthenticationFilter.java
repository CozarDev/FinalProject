package com.proyectofinal.backend.Security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.proyectofinal.backend.Services.JWTService;
import com.proyectofinal.backend.Models.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.proyectofinal.backend.Controllers.AuthController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthController authController;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Añadimos CORS headers para solucionar problemas de acceso
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "authorization, content-type, xsrf-token");
        response.setHeader("Access-Control-Expose-Headers", "xsrf-token");
        
        // Manejo específico para OPTIONS (preflight requests)
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            chain.doFilter(request, response);
            return;
        }

        // Obtener el encabezado de autorización del request
        String header = request.getHeader("Authorization");
        String username = null;
        String jwt = null;
        String role = null;

        logger.info("DEBUG JWT - Request: {} {}", request.getMethod(), request.getRequestURI());
        logger.info("DEBUG JWT - Authorization header: {}", header != null ? "Present" : "Missing");

        if (header != null && header.startsWith("Bearer ")) {
            jwt = header.substring(7);
            logger.debug("DEBUG JWT - Token extracted successfully");

            // Verificar si el token está revocado
            if (authController.isTokenRevoked(jwt)) {
                logger.warn("DEBUG JWT - Token is revoked");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token revocado");
                return;
            }

            try {
                // Extraer información del token JWT
                username = jwtService.extractUsername(jwt);
                role = jwtService.extractRole(jwt);
                logger.info("DEBUG JWT - Username: {}, Role: {}", username, role);
            } catch (Exception e) {
                logger.error("DEBUG JWT - Error al procesar el token JWT", e);
            }
        } else {
            logger.info("DEBUG JWT - No Bearer token found");
        }

        // Validar el token y establecer la autenticación en el contexto de seguridad
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("DEBUG JWT - Attempting to load user: {}", username);
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            logger.info("DEBUG JWT - User loaded successfully: {}", userDetails.getUsername());

            if (jwtService.validateToken(jwt, userDetails.getUsername())) {
                logger.info("DEBUG JWT - Token validation successful for user: {}", username);
                // Crear manualmente las autoridades si es necesario
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (role != null && !role.isEmpty()) {
                    // Añadir ambas formas de la autoridad
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    authorities.add(new SimpleGrantedAuthority(role));
                }
                
                // Si userDetails ya tiene autoridades, usarlas en lugar de las creadas manualmente
                UsernamePasswordAuthenticationToken authentication;
                if (userDetails.getAuthorities() != null && !userDetails.getAuthorities().isEmpty()) {
                    authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                } else {
                    authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, authorities);
                }
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Añadir información de debug
                logger.info("DEBUG JWT - Usuario autenticado: {}, Rol: {}", username, role);
                logger.info("DEBUG JWT - Authorities: {}", authentication.getAuthorities());
            }
        }
        
        chain.doFilter(request, response);
    }
} 