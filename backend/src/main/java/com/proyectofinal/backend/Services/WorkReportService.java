package com.proyectofinal.backend.Services;

import com.proyectofinal.backend.Models.Employee;
import com.proyectofinal.backend.Models.WorkReport;
import com.proyectofinal.backend.Repositories.EmployeeRepository;
import com.proyectofinal.backend.Repositories.WorkReportRepository;
import com.proyectofinal.backend.Requests.WorkReportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkReportService {

    @Autowired
    private WorkReportRepository workReportRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Crear un nuevo parte de trabajo
    public WorkReport createWorkReport(String employeeId, WorkReportRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // Verificar si ya existe un parte para esta fecha
        if (workReportRepository.existsByEmployeeIdAndReportDate(employeeId, request.getReportDate())) {
            throw new RuntimeException("Ya existe un parte de trabajo para esta fecha");
        }

        WorkReport workReport = new WorkReport(
                employeeId,
                request.getReportDate(),
                request.getStartTime(),
                request.getEndTime(),
                request.getBreakDuration(),
                request.getObservations()
        );

        return workReportRepository.save(workReport);
    }

    // Obtener todos los partes (solo admin)
    public List<WorkReport> getAllWorkReports() {
        return workReportRepository.findAllByOrderByCreatedAtDesc();
    }

    // Obtener partes de un empleado específico
    public List<WorkReport> getWorkReportsByEmployee(String employeeId) {
        return workReportRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    // Obtener partes de empleados de un departamento específico (para jefes de departamento)
    public List<WorkReport> getWorkReportsByDepartment(String departmentId) {
        // Primero obtenemos todos los empleados del departamento
        List<Employee> departmentEmployees = employeeRepository.findByDepartmentId(departmentId);
        List<String> employeeIds = departmentEmployees.stream()
                .map(Employee::getId)
                .toList();
        
        return workReportRepository.findByEmployeeIdInOrderByCreatedAtDesc(employeeIds);
    }

    // Obtener un parte específico por ID
    public Optional<WorkReport> getWorkReportById(String id) {
        return workReportRepository.findById(id);
    }

    // Actualizar un parte de trabajo (solo admin)
    public WorkReport updateWorkReport(String id, WorkReportRequest request) {
        WorkReport workReport = workReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Parte de trabajo no encontrado"));

        workReport.setReportDate(request.getReportDate());
        workReport.setStartTime(request.getStartTime());
        workReport.setEndTime(request.getEndTime());
        workReport.setBreakDuration(request.getBreakDuration());
        workReport.setObservations(request.getObservations());
        workReport.preUpdate();

        return workReportRepository.save(workReport);
    }

    // Eliminar un parte de trabajo (solo admin)
    public void deleteWorkReport(String id) {
        if (!workReportRepository.existsById(id)) {
            throw new RuntimeException("Parte de trabajo no encontrado");
        }
        workReportRepository.deleteById(id);
    }

    // Verificar si un empleado puede ver un parte específico
    public boolean canEmployeeViewReport(String employeeId, String reportId, String userRole) {
        Optional<WorkReport> workReport = workReportRepository.findById(reportId);
        if (workReport.isEmpty()) {
            return false;
        }

        // Admin puede ver todo
        if ("ADMIN".equals(userRole)) {
            return true;
        }

        WorkReport report = workReport.get();
        
        // El empleado puede ver sus propios partes
        if (report.getEmployeeId().equals(employeeId)) {
            return true;
        }

        // Jefe de departamento puede ver partes de su departamento
        if ("DEPARTMENT_HEAD".equals(userRole)) {
            Employee currentEmployee = employeeRepository.findById(employeeId).orElse(null);
            if (currentEmployee != null) {
                Employee reportEmployee = employeeRepository.findById(report.getEmployeeId()).orElse(null);
                if (reportEmployee != null && 
                    currentEmployee.getDepartmentId().equals(reportEmployee.getDepartmentId())) {
                    return true;
                }
            }
        }

        return false;
    }

    // Obtener partes según el rol del usuario
    public List<WorkReport> getWorkReportsForUser(String employeeId, String userRole) {
        if ("ADMIN".equals(userRole)) {
            return getAllWorkReports();
        } else if ("DEPARTMENT_HEAD".equals(userRole)) {
            Employee employee = employeeRepository.findById(employeeId).orElse(null);
            if (employee != null) {
                return getWorkReportsByDepartment(employee.getDepartmentId());
            }
        }
        
        // Empleado normal solo ve sus propios partes
        return getWorkReportsByEmployee(employeeId);
    }
} 