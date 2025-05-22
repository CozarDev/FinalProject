package com.proyectofinal.frontend.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyectofinal.frontend.Models.Department;
import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_EMPLOYEE = 1;
    
    private List<Object> items = new ArrayList<>();
    private Map<String, String> departmentMap = new HashMap<>(); // Mapa de departmentId -> departmentName
    private OnEmployeeClickListener listener;
    
    public interface OnEmployeeClickListener {
        void onEditEmployee(Employee employee, int position);
        void onDeleteEmployee(Employee employee, int position);
    }
    
    public EmployeeAdapter(OnEmployeeClickListener listener) {
        this.listener = listener;
    }
    
    public void setEmployees(List<Employee> employees, Map<String, String> departmentMap) {
        this.departmentMap = departmentMap;
        items.clear();
        
        // Agrupar empleados por departamento
        Map<String, List<Employee>> groupedEmployees = new HashMap<>();
        
        for (Employee employee : employees) {
            String departmentId = employee.getDepartmentId();
            String departmentName = departmentMap.getOrDefault(departmentId, "Sin departamento");
            
            // Asignar nombre de departamento al empleado para mostrar
            employee.setDepartmentName(departmentName);
            
            if (!groupedEmployees.containsKey(departmentId)) {
                groupedEmployees.put(departmentId, new ArrayList<>());
            }
            groupedEmployees.get(departmentId).add(employee);
        }
        
        // Ordenar departamentos por nombre
        List<String> departmentIds = new ArrayList<>(groupedEmployees.keySet());
        Collections.sort(departmentIds, (id1, id2) -> {
            String name1 = departmentMap.getOrDefault(id1, "Sin departamento");
            String name2 = departmentMap.getOrDefault(id2, "Sin departamento");
            return name1.compareToIgnoreCase(name2);
        });
        
        // Construir la lista final con headers y empleados
        for (String departmentId : departmentIds) {
            String departmentName = departmentMap.getOrDefault(departmentId, "Sin departamento");
            items.add(departmentName); // Header
            
            // Ordenar empleados por nombre
            List<Employee> employeesInDepartment = groupedEmployees.get(departmentId);
            Collections.sort(employeesInDepartment, (e1, e2) -> 
                e1.getFullName().compareToIgnoreCase(e2.getFullName()));
            
            items.addAll(employeesInDepartment);
        }
        
        notifyDataSetChanged();
    }
    
    public void updateEmployee(int position, Employee employee) {
        if (position >= 0 && position < items.size() && items.get(position) instanceof Employee) {
            items.set(position, employee);
            notifyItemChanged(position);
        }
    }
    
    public void removeEmployee(int position) {
        if (position >= 0 && position < items.size() && items.get(position) instanceof Employee) {
            items.remove(position);
            notifyItemRemoved(position);
            
            // Si ya no hay empleados después del header, eliminar el header también
            if (position > 0 && items.get(position - 1) instanceof String &&
                (position >= items.size() || items.get(position) instanceof String)) {
                items.remove(position - 1);
                notifyItemRemoved(position - 1);
            }
        }
    }
    
    /**
     * Añade un nuevo empleado a la lista.
     * Buscará el departamento correspondiente o creará una nueva sección.
     * @param employee El empleado a añadir
     */
    public void addEmployee(Employee employee) {
        if (employee == null) return;

        String departmentId = employee.getDepartmentId();
        String departmentName = departmentMap.getOrDefault(departmentId, "Sin departamento");
        
        // Buscar si ya existe un header para este departamento
        int headerPosition = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) instanceof String && items.get(i).equals(departmentName)) {
                headerPosition = i;
                break;
            }
        }
        
        if (headerPosition >= 0) {
            // Departamento existente, añadir empleado a este grupo
            // Buscar posición donde insertar (orden alfabético por nombre)
            int insertPosition = headerPosition + 1;
            while (insertPosition < items.size() && 
                   items.get(insertPosition) instanceof Employee && 
                   ((Employee)items.get(insertPosition)).getFullName().compareToIgnoreCase(employee.getFullName()) < 0) {
                insertPosition++;
            }
            
            items.add(insertPosition, employee);
            notifyItemInserted(insertPosition);
        } else {
            // Crear nueva sección para este departamento
            // Buscar la posición correcta para el nuevo departamento (orden alfabético)
            int insertPosition = 0;
            while (insertPosition < items.size() && 
                   items.get(insertPosition) instanceof String && 
                   ((String)items.get(insertPosition)).compareToIgnoreCase(departmentName) < 0) {
                // Saltar este departamento y todos sus empleados
                insertPosition++;
                while (insertPosition < items.size() && items.get(insertPosition) instanceof Employee) {
                    insertPosition++;
                }
            }
            
            // Insertar header y empleado
            items.add(insertPosition, departmentName);
            items.add(insertPosition + 1, employee);
            notifyItemRangeInserted(insertPosition, 2);
        }
    }
    
    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_EMPLOYEE;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_department_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_employee, parent, false);
            return new EmployeeViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            String departmentName = (String) items.get(position);
            headerHolder.bind(departmentName);
        } else if (holder instanceof EmployeeViewHolder) {
            EmployeeViewHolder employeeHolder = (EmployeeViewHolder) holder;
            Employee employee = (Employee) items.get(position);
            employeeHolder.bind(employee, position);
        }
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    // Header ViewHolder
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView departmentNameTextView;
        
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            departmentNameTextView = itemView.findViewById(R.id.departmentNameTextView);
        }
        
        void bind(String departmentName) {
            departmentNameTextView.setText(departmentName);
        }
    }
    
    // Employee ViewHolder
    class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private TextView employeeNameTextView;
        private TextView employeeEmailTextView;
        private ImageButton editButton;
        private ImageButton deleteButton;
        
        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            employeeNameTextView = itemView.findViewById(R.id.employeeNameTextView);
            employeeEmailTextView = itemView.findViewById(R.id.employeeEmailTextView);
            editButton = itemView.findViewById(R.id.editEmployeeButton);
            deleteButton = itemView.findViewById(R.id.deleteEmployeeButton);
        }
        
        void bind(Employee employee, int position) {
            employeeNameTextView.setText(employee.getFullName());
            employeeEmailTextView.setText(employee.getEmail());
            
            // Configurar listeners para botones
            editButton.setOnClickListener(v -> 
                listener.onEditEmployee(employee, position));
            
            deleteButton.setOnClickListener(v -> 
                listener.onDeleteEmployee(employee, position));
        }
    }
} 