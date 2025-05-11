package com.proyectofinal.backend.Controllers;

import com.proyectofinal.backend.Models.Department;
import com.proyectofinal.backend.Repositories.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // Devuelve una lista de todos los departamentos
    @GetMapping
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Crea un nuevo departamento y lo guarda en la base de datos
    @PostMapping
    public Department createDepartment(@RequestBody Department department) {
        return departmentRepository.save(department);
    }

    // Actualiza un departamento existente basado en su ID
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(@PathVariable String id, @RequestBody Department departmentDetails) {
        Optional<Department> departmentOpt = departmentRepository.findById(id);
        if (departmentOpt.isPresent()) {
            Department department = departmentOpt.get();
            department.setName(departmentDetails.getName());
            department.setDescription(departmentDetails.getDescription());
            // Actualiza otros campos según sea necesario
            return ResponseEntity.ok(departmentRepository.save(department));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Elimina un departamento basado en su ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable String id) {
        if (departmentRepository.existsById(id)) {
            departmentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 