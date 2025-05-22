package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.ShiftType;
import com.proyectofinal.backend.Repositories.ShiftTypeRepository;
import com.proyectofinal.backend.Services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shifttypes")
public class ShiftTypeController {

    private final ShiftTypeRepository shiftTypeRepository;
    private final UserService userService;

    @Autowired
    public ShiftTypeController(ShiftTypeRepository shiftTypeRepository, UserService userService) {
        this.shiftTypeRepository = shiftTypeRepository;
        this.userService = userService;
    }

    // Obtener todos los tipos de turno
    @GetMapping
    public ResponseEntity<List<ShiftType>> getAllShiftTypes() {
        // Permitimos acceso a todos los usuarios autenticados (incluidos jefes de departamento)
        return ResponseEntity.ok(shiftTypeRepository.findAll());
    }

    // Obtener un tipo de turno por ID
    @GetMapping("/{id}")
    public ResponseEntity<ShiftType> getShiftTypeById(@PathVariable String id) {
        Optional<ShiftType> shiftType = shiftTypeRepository.findById(id);
        return shiftType.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Crear un nuevo tipo de turno
    @PostMapping
    public ResponseEntity<?> createShiftType(@RequestBody ShiftType shiftType) {
        // Verificar permisos (solo admin puede crear tipos de turno)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo administradores pueden crear tipos de turno");
        }
        
        ShiftType savedShiftType = shiftTypeRepository.save(shiftType);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedShiftType);
    }

    // Actualizar un tipo de turno existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateShiftType(@PathVariable String id, @RequestBody ShiftType shiftTypeDetails) {
        // Verificar permisos (solo admin puede actualizar tipos de turno)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo administradores pueden actualizar tipos de turno");
        }
        
        Optional<ShiftType> shiftTypeOpt = shiftTypeRepository.findById(id);
        
        if (!shiftTypeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        ShiftType shiftType = shiftTypeOpt.get();
        shiftType.setName(shiftTypeDetails.getName());
        shiftType.setStartTime(shiftTypeDetails.getStartTime());
        shiftType.setEndTime(shiftTypeDetails.getEndTime());
        shiftType.setColor(shiftTypeDetails.getColor());
        shiftType.setWorkDays(shiftTypeDetails.getWorkDays());
        
        return ResponseEntity.ok(shiftTypeRepository.save(shiftType));
    }

    // Eliminar un tipo de turno
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShiftType(@PathVariable String id) {
        // Verificar permisos (solo admin puede eliminar tipos de turno)
        if (!userService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Solo administradores pueden eliminar tipos de turno");
        }
        
        if (!shiftTypeRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        
        shiftTypeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
} 