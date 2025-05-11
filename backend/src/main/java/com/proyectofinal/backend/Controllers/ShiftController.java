package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.Shift;
import com.proyectofinal.backend.Repositories.ShiftRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shifts")
public class ShiftController {

    private final ShiftRepository shiftRepository;

    public ShiftController(ShiftRepository shiftRepository) {
        this.shiftRepository = shiftRepository;
    }

    // Devuelve una lista de todos los turnos
    @GetMapping
    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    // Crea un nuevo turno y lo guarda en la base de datos
    @PostMapping
    public Shift createShift(@RequestBody Shift shift) {
        return shiftRepository.save(shift);
    }

    // Actualiza un turno existente basado en su ID
    @PutMapping("/{id}")
    public ResponseEntity<Shift> updateShift(@PathVariable String id, @RequestBody Shift shiftDetails) {
        Optional<Shift> shiftOpt = shiftRepository.findById(id);
        if (shiftOpt.isPresent()) {
            Shift shift = shiftOpt.get();
            shift.setDate(shiftDetails.getDate());
            shift.setShiftType(shiftDetails.getShiftType());
            // Actualiza otros campos según sea necesario
            return ResponseEntity.ok(shiftRepository.save(shift));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Elimina un turno basado en su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShift(@PathVariable String id) {
        if (shiftRepository.existsById(id)) {
            shiftRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 