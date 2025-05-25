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
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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
     * NUEVO: Implementa rotación basada en las vacaciones del año anterior.
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
            totalAssigned += assignVacationsForDepartmentWithRotation(dept.getId(), year, false);
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
        return assignVacationsForDepartmentWithRotation(departmentId, year, true);
    }
    
    /**
     * Asigna vacaciones automáticamente a los empleados de un departamento específico (método original)
     * @param departmentId El ID del departamento
     * @param year El año para el que se asignarán las vacaciones
     * @param deleteExisting Si debe eliminar las vacaciones existentes para este departamento
     * @return Número de empleados a los que se les asignaron vacaciones
     */
    public int assignVacationsForDepartment(String departmentId, int year, boolean deleteExisting) {
        // Usar el nuevo método con rotación por defecto
        return assignVacationsForDepartmentWithRotation(departmentId, year, deleteExisting);
    }
    
    /**
     * NUEVO: Asigna vacaciones con sistema de rotación basado en el año anterior
     * @param departmentId El ID del departamento
     * @param year El año para el que se asignarán las vacaciones
     * @param deleteExisting Si debe eliminar las vacaciones existentes para este departamento
     * @return Número de empleados a los que se les asignaron vacaciones
     */
    public int assignVacationsForDepartmentWithRotation(String departmentId, int year, boolean deleteExisting) {
        // 1. Obtener todos los empleados del departamento
        List<Employee> employees = employeeRepository.findByDepartmentId(departmentId);
        
        if (employees.isEmpty()) {
            return 0;
        }
        
        // 2. Eliminar las vacaciones existentes para este año y departamento
        if (deleteExisting) {
            deleteExistingVacations(departmentId, year);
        }
        
        // 3. Obtener el historial de vacaciones del año anterior para rotar
        Map<String, Integer> lastYearVacationMonths = getLastYearVacationMonths(employees, year - 1);
        
        // 4. Crear estructura para distribuir empleados por mes con rotación
        Map<Integer, List<Employee>> monthAssignments = createRotatedMonthAssignments(employees, lastYearVacationMonths);
        
        // 5. Guardar las asignaciones en la base de datos
        int totalAssigned = 0;
        for (Map.Entry<Integer, List<Employee>> entry : monthAssignments.entrySet()) {
            int month = entry.getKey();
            List<Employee> monthEmployees = entry.getValue();
            
            for (Employee emp : monthEmployees) {
                // Crear excepción de tipo VACATION para todo el mes
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.plusMonths(1).minusDays(1);
                
                ShiftException vacation = new ShiftException();
                vacation.setEmployeeId(emp.getId());
                vacation.setStartDate(Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                vacation.setEndDate(Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
                vacation.setType("VACATION");
                vacation.setDescription("Vacaciones anuales " + year + " (rotación mes " + month + ")");
                vacation.setAutoGenerated(true);
                
                shiftExceptionRepository.save(vacation);
                totalAssigned++;
            }
        }
        
        return totalAssigned;
    }
    
    /**
     * Obtiene los meses de vacaciones del año anterior para cada empleado
     * @param employees Lista de empleados
     * @param previousYear Año anterior a consultar
     * @return Mapa de empleadoId -> mes de vacaciones (1-12)
     */
    private Map<String, Integer> getLastYearVacationMonths(List<Employee> employees, int previousYear) {
        Map<String, Integer> vacationMonths = new HashMap<>();
        
        // Obtener las fechas del año anterior
        LocalDate startOfYear = LocalDate.of(previousYear, 1, 1);
        LocalDate endOfYear = LocalDate.of(previousYear, 12, 31);
        Date start = Date.from(startOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endOfYear.atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        for (Employee emp : employees) {
            // Buscar vacaciones del empleado en el año anterior
            List<ShiftException> vacations = shiftExceptionRepository.findByEmployeeIdAndTypeAndDateRange(
                    emp.getId(), "VACATION", start, end);
            
            if (!vacations.isEmpty()) {
                // Tomar la primera vacación encontrada y obtener su mes
                ShiftException firstVacation = vacations.get(0);
                LocalDate vacationDate = firstVacation.getStartDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                int month = vacationDate.getMonthValue();
                vacationMonths.put(emp.getId(), month);
            }
            // Si no tiene vacaciones el año anterior, no se añade al mapa (empleado nuevo)
        }
        
        return vacationMonths;
    }
    
    /**
     * Crea las asignaciones mensuales con sistema de rotación
     * @param employees Lista de empleados del departamento
     * @param lastYearVacationMonths Meses de vacaciones del año anterior
     * @return Mapa de mes -> lista de empleados asignados
     */
    private Map<Integer, List<Employee>> createRotatedMonthAssignments(List<Employee> employees, 
                                                                      Map<String, Integer> lastYearVacationMonths) {
        Map<Integer, List<Employee>> monthAssignments = new HashMap<>();
        
        // Inicializar listas para cada mes
        for (int month = 1; month <= 12; month++) {
            monthAssignments.put(month, new ArrayList<>());
        }
        
        // Separar empleados: con historial y nuevos
        List<Employee> employeesWithHistory = new ArrayList<>();
        List<Employee> newEmployees = new ArrayList<>();
        
        for (Employee emp : employees) {
            if (lastYearVacationMonths.containsKey(emp.getId())) {
                employeesWithHistory.add(emp);
            } else {
                newEmployees.add(emp);
            }
        }
        
        // 1. Asignar empleados con historial aplicando rotación (+ 3 meses)
        for (Employee emp : employeesWithHistory) {
            int lastYearMonth = lastYearVacationMonths.get(emp.getId());
            int newMonth = ((lastYearMonth + 2) % 12) + 1; // Rotar 3 meses
            monthAssignments.get(newMonth).add(emp);
        }
        
        // 2. Distribuir empleados nuevos en los meses con menos asignados
        Collections.sort(newEmployees, Comparator.comparing(Employee::getId)); // Orden consistente
        
        for (Employee newEmp : newEmployees) {
            // Encontrar el mes con menos empleados asignados
            int monthWithFewest = 1;
            int minCount = monthAssignments.get(1).size();
            
            for (int month = 2; month <= 12; month++) {
                int currentCount = monthAssignments.get(month).size();
                if (currentCount < minCount) {
                    minCount = currentCount;
                    monthWithFewest = month;
                }
            }
            
            monthAssignments.get(monthWithFewest).add(newEmp);
        }
        
        return monthAssignments;
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