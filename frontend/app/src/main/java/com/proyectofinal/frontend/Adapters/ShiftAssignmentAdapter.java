package com.proyectofinal.frontend.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyectofinal.frontend.Models.Department;
import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.Models.ShiftAssignment;
import com.proyectofinal.frontend.Models.ShiftType;
import com.proyectofinal.frontend.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftAssignmentAdapter extends RecyclerView.Adapter<ShiftAssignmentAdapter.ShiftAssignmentViewHolder> {

    private List<ShiftAssignment> shiftAssignments;
    private Context context;
    private Map<String, Employee> employeeMap;
    private Map<String, Department> departmentMap;
    private Map<String, ShiftType> shiftTypeMap;
    private OnShiftAssignmentListener listener;

    // Interfaz para manejar eventos de clic
    public interface OnShiftAssignmentListener {
        void onEditClick(ShiftAssignment shiftAssignment, int position);
        void onDeleteClick(ShiftAssignment shiftAssignment, int position);
    }

    public ShiftAssignmentAdapter(List<ShiftAssignment> shiftAssignments, Context context, 
                                 Map<String, Employee> employeeMap, 
                                 Map<String, Department> departmentMap,
                                 Map<String, ShiftType> shiftTypeMap,
                                 OnShiftAssignmentListener listener) {
        this.shiftAssignments = shiftAssignments;
        this.context = context;
        this.employeeMap = employeeMap != null ? employeeMap : new HashMap<>();
        this.departmentMap = departmentMap != null ? departmentMap : new HashMap<>();
        this.shiftTypeMap = shiftTypeMap != null ? shiftTypeMap : new HashMap<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShiftAssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shift_assignment, parent, false);
        return new ShiftAssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShiftAssignmentViewHolder holder, int position) {
        ShiftAssignment assignment = shiftAssignments.get(position);
        
        // Configurar datos del empleado
        Employee employee = employeeMap.get(assignment.getEmployeeId());
        if (employee != null) {
            holder.tvEmployeeName.setText(employee.getFirstName() + " " + employee.getLastName());
            
            // Configurar datos del departamento
            Department department = departmentMap.get(employee.getDepartmentId());
            if (department != null) {
                holder.tvDepartmentName.setText(department.getName());
            } else {
                holder.tvDepartmentName.setText("Departamento no disponible");
            }
        } else {
            holder.tvEmployeeName.setText("Empleado no disponible");
            holder.tvDepartmentName.setText("Departamento no disponible");
        }
        
        // Configurar datos del tipo de turno
        ShiftType shiftType = shiftTypeMap.get(assignment.getShiftTypeId());
        if (shiftType != null) {
            holder.tvShiftType.setText(shiftType.getName());
            holder.tvShiftTime.setText(shiftType.getStartTime() + " - " + shiftType.getEndTime());
            
            // Establecer color según el tipo de turno
            try {
                holder.colorIndicator.setBackgroundColor(Color.parseColor(shiftType.getColor()));
            } catch (Exception e) {
                // Si hay un error con el color, usar un color por defecto
                holder.colorIndicator.setBackgroundColor(Color.GRAY);
            }
        } else {
            holder.tvShiftType.setText("Tipo de turno no disponible");
            holder.tvShiftTime.setText("Horario no disponible");
            holder.colorIndicator.setBackgroundColor(Color.GRAY);
        }
        
        // Configurar listeners para los botones
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(assignment, holder.getAdapterPosition());
            }
        });
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(assignment, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return shiftAssignments != null ? shiftAssignments.size() : 0;
    }
    
    // Método para actualizar el conjunto de datos
    public void updateData(List<ShiftAssignment> newAssignments) {
        this.shiftAssignments = newAssignments;
        notifyDataSetChanged();
    }
    
    // Método para actualizar los mapas de datos
    public void updateMaps(Map<String, Employee> employeeMap, 
                          Map<String, Department> departmentMap,
                          Map<String, ShiftType> shiftTypeMap) {
        this.employeeMap = employeeMap != null ? employeeMap : this.employeeMap;
        this.departmentMap = departmentMap != null ? departmentMap : this.departmentMap;
        this.shiftTypeMap = shiftTypeMap != null ? shiftTypeMap : this.shiftTypeMap;
        notifyDataSetChanged();
    }

    // ViewHolder para las vistas de cada ítem
    public static class ShiftAssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeName, tvDepartmentName, tvShiftType, tvShiftTime;
        View colorIndicator;
        Button btnEdit, btnDelete;

        public ShiftAssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvDepartmentName = itemView.findViewById(R.id.tvDepartmentName);
            tvShiftType = itemView.findViewById(R.id.tvShiftType);
            tvShiftTime = itemView.findViewById(R.id.tvShiftTime);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
} 