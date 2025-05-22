package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.Department;
import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.ShiftException;
import com.proyectofinal.backend.Repositories.DepartmentRepository;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.ShiftExceptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
public class VacationService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftExceptionRepository shiftExceptionRepository;

    public VacationService(
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            ShiftExceptionRepository shiftExceptionRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.shiftExceptionRepository = shiftExceptionRepository;
    }

    /**
     * Asigna vacaciones automáticamente a todos los empleados para un año específico.
     * Primero elimina todas las vacaciones existentes para ese año.
     * @param year El año para el que se asignarán las vacaciones
     * @return Número de empleados a los que se les asignaron vacaciones
     */
    public int assignVacationsForYear(int year) {
        // Primero eliminamos todas las vacaciones existentes para este año
        deleteAllVacationsForYear(year);
        
        // Obtener todos los departamentos
        List<Department> departments = departmentRepository.findAll();
        int totalAssigned = 0;
        
        for (Department dept : departments) {
            totalAssigned += assignVacationsForDepartment(dept.getId(), year, false);
        }
        
        return totalAssigned;
    }
    
    /**
     * Elimina todas las vacaciones para un año específico
     * @param year El año para el que se eliminarán las vacaciones
     * @return Número de vacaciones eliminadas
     */
    public int deleteAllVacationsForYear(int year) {
        // Obtener las fechas de inicio y fin del año
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        Date start = Date.from(startOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        // Buscar todas las vacaciones para este año
        List<ShiftException> vacations = shiftExceptionRepository.findByTypeAndDateRange("VACATION", start, end);
        
        int count = vacations.size();
        shiftExceptionRepository.deleteAll(vacations);
        
        return count;
    }
    
    /**
     * Asigna vacaciones automáticamente a los empleados de un departamento específico
     * @param departmentId El ID del departamento
     * @param year El año para el que se asignarán las vacaciones
     * @return Número de empleados a los que se les asignaron vacaciones
     */
    public int assignVacationsForDepartment(String departmentId, int year) {
        return assignVacationsForDepartment(departmentId, year, true);
    }
    
    /**
     * Asigna vacaciones automáticamente a los empleados de un departamento específico
     * @param departmentId El ID del departamento
     * @param year El año para el que se asignarán las vacaciones
     * @param deleteExisting Si debe eliminar las vacaciones existentes para este departamento
     * @return Número de empleados a los que se les asignaron vacaciones
     */
    public int assignVacationsForDepartment(String departmentId, int year, boolean deleteExisting) {
        // 1. Obtener todos los empleados del departamento
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        
        if (employees.isEmpty()) {
            return 0;
        }
        
        // 2. Ordenar empleados por ID (o cualquier otro criterio que prefieras)
        Collections.sort(employees, Comparator.comparing(Employee::getId));
        
        // 3. Calcular cuántos empleados se asignarán a cada mes
        int employeesPerMonth = Math.max(1, employees.size() / 12);
        int remainingEmployees = employees.size() % 12;
        
        // 4. Eliminar las vacaciones existentes para este año y departamento
        if (deleteExisting) {
            deleteExistingVacations(departmentId, year);
        }
        
        // 5. Asignar vacaciones mes a mes
        int employeeIndex = 0;
        int totalAssigned = 0;
        
        for (int month = 1; month <= 12 && employeeIndex < employees.size(); month++) {
            // Calcular cuántos empleados se asignarán este mes
            int empCountThisMonth = employeesPerMonth;
            if (remainingEmployees > 0) {
                empCountThisMonth++;
                remainingEmployees--;
            }
            
            // Asignar a cada empleado del mes
            for (int i = 0; i < empCountThisMonth && employeeIndex < employees.size(); i++) {
                Employee emp = employees.get(employeeIndex++);
                
                // Crear excepción de tipo VACATION para todo el mes
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.plusMonths(1).minusDays(1);
                
                ShiftException vacation = new ShiftException();
                vacation.setEmployeeId(emp.getId());
                vacation.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                vacation.setEndDate(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                vacation.setType("VACATION");
                vacation.setDescription("Vacaciones anuales " + year);
                vacation.setAutoGenerated(true);
                
                shiftExceptionRepository.save(vacation);
                totalAssigned++;
            }
        }
        
        return totalAssigned;
    }
    
    /**
     * Elimina las vacaciones existentes para un departamento y año específicos
     */
    private void deleteExistingVacations(String departmentId, int year) {
        // 1. Obtener todos los empleados del departamento
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        
        // 2. Obtener las fechas de inicio y fin del año
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        Date start = Date.from(startOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        // 3. Para cada empleado, buscar y eliminar sus vacaciones existentes
        for (Employee emp : employees) {
            List<ShiftException> vacations = shiftExceptionRepository.findByEmployeeIdAndTypeAndDateRange(
                    emp.getId(), "VACATION", start, end);
            
            shiftExceptionRepository.deleteAll(vacations);
        }
    }
} 