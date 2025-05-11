package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Repositories.UserRepository;
import com.proyectofinal.backend.Requests.AuthRequest;
import com.proyectofinal.backend.Services.JWTService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Por si quieres hacer pruebas desde Android/local
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final Set<String> revokedTokens = new HashSet<>();

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Maneja el proceso de inicio de sesión y genera un token JWT si las credenciales son correctas
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody AuthRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }

        String token = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(token);
    }

    // Maneja el proceso de cierre de sesión y revoca el token JWT
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String token) {
        // Eliminar el prefijo "Bearer " del token
        String jwtToken = token.replace("Bearer ", "");
        // Añadir el token a la lista de tokens revocados
        revokedTokens.add(jwtToken);
        return ResponseEntity.ok("Logout exitoso");
    }

    // Método para verificar si un token está revocado
    public boolean isTokenRevoked(String token) {
        return revokedTokens.contains(token);
    }
}
