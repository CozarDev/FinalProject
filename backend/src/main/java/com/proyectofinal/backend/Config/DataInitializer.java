package com.proyectofinal.backend.Config;

import com.proyectofinal.backend.Models.Department;
import com.proyectofinal.backend.Models.User;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Repositories.DepartmentRepository;
import com.proyectofinal.backend.Repositories.UserRepository;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 Iniciando inicialización de datos del sistema...");
        
        // 1. Inicializar usuario administrador y empleado0 (siempre primero)
        initializeAdminUser();
        
        // 2. Inicializar departamento de Incidencias
        initializeIncidencesDepartment();
        
        logger.info("✅ Inicialización de datos completada exitosamente.");
    }

    /**
     * Inicializa el usuario administrador y empleado0 si no existe
     */
    private void initializeAdminUser() {
        try {
            // Verificar si ya existe el usuario admin
            if (userRepository.findByUsername("admin").isPresent()) {
                logger.info("El usuario administrador ya existe.");
                return;
            }
            
            // Crear usuario admin
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Cambiar por una más segura en producción
            admin.setRole("ADMIN");
            User savedAdmin = userRepository.save(admin);
            
            // Crear empleado0 asociado al admin
            Employee employee0 = new Employee();
            employee0.setFirstName("Empleado");
            employee0.setLastName("Cero");
            employee0.setEmail("empleado0@empresa.com");
            employee0.setUserId(savedAdmin.getId());
            Employee savedEmployee = employeeRepository.save(employee0);
            
            logger.info("✅ Usuario administrador y empleado0 creados exitosamente:");
            logger.info("   - Username: admin");
            logger.info("   - Password: admin123 (⚠️ cambiar en producción)");
            logger.info("   - Role: ADMIN");
            logger.info("   - Empleado ID: {}", savedEmployee.getId());
            logger.info("   - Email empleado: {}", savedEmployee.getEmail());
            
        } catch (Exception e) {
            logger.error("❌ Error al inicializar el usuario administrador: {}", e.getMessage(), e);
        }
    }

    /**
     * Inicializa el departamento de Incidencias si no existe
     */
    private void initializeIncidencesDepartment() {
        try {
            // Verificar si ya existe un departamento de Incidencias
            Department existingIncidencesDept = departmentRepository.findByNameIgnoreCase("Incidencias");
            
            if (existingIncidencesDept != null) {
                logger.info("El departamento de Incidencias ya existe con ID: {}", existingIncidencesDept.getId());
                return;
            }
            
            // Crear el departamento de Incidencias
            Department incidencesDepartment = new Department();
            incidencesDepartment.setName("Incidencias");
            incidencesDepartment.setDescription("Departamento encargado de gestionar y resolver las incidencias técnicas y operativas de la empresa. Los empleados de este departamento se encargan de atender, procesar y solucionar todas las incidencias reportadas por el resto de empleados.");
            incidencesDepartment.setManagerId(null); // Sin jefe inicialmente, se puede asignar después como admin
            
            Department savedDepartment = departmentRepository.save(incidencesDepartment);
            
            logger.info("✅ Departamento de Incidencias creado exitosamente:");
            logger.info("   - ID: {}", savedDepartment.getId());
            logger.info("   - Nombre: {}", savedDepartment.getName());
            logger.info("   - Descripción: {}", savedDepartment.getDescription());
            logger.info("   - Jefe de departamento: No asignado (se puede configurar posteriormente)");
            
        } catch (Exception e) {
            logger.error("❌ Error al inicializar el departamento de Incidencias: {}", e.getMessage(), e);
        }
    }
} 