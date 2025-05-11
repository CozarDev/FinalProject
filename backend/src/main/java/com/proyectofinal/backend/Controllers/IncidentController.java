package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.Incident;
import com.proyectofinal.backend.Repositories.IncidentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;

    public IncidentController(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    // Devuelve una lista de todos los incidentes
    @GetMapping
    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    // Crea un nuevo incidente y lo guarda en la base de datos
    @PostMapping
    public Incident createIncident(@RequestBody Incident incident) {
        return incidentRepository.save(incident);
    }

    // Actualiza un incidente existente basado en su ID
    @PutMapping("/{id}")
    public ResponseEntity<Incident> updateIncident(@PathVariable String id, @RequestBody Incident incidentDetails) {
        Optional<Incident> incidentOpt = incidentRepository.findById(id);
        if (incidentOpt.isPresent()) {
            Incident incident = incidentOpt.get();
            incident.setTitle(incidentDetails.getTitle());
            incident.setDescription(incidentDetails.getDescription());
            // Actualiza otros campos según sea necesario
            return ResponseEntity.ok(incidentRepository.save(incident));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Elimina un incidente basado en su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable String id) {
        if (incidentRepository.existsById(id)) {
            incidentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 