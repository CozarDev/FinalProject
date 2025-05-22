package com.proyectofinal.frontend.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyectofinal.frontend.Models.Employee;
import com.proyectofinal.frontend.R;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSearchAdapter extends RecyclerView.Adapter<EmployeeSearchAdapter.EmployeeViewHolder> {
    
    private List<Employee> employees = new ArrayList<>();
    private OnEmployeeSelectedListener listener;
    
    public interface OnEmployeeSelectedListener {
        void onEmployeeSelected(Employee employee);
    }
    
    public EmployeeSearchAdapter(OnEmployeeSelectedListener listener) {
        this.listener = listener;
    }
    
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_employee_search, parent, false);
        return new EmployeeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee employee = employees.get(position);
        holder.bind(employee);
    }
    
    @Override
    public int getItemCount() {
        return employees.size();
    }
    
    class EmployeeViewHolder extends RecyclerView.ViewHolder {
        
        private TextView employeeNameTextView;
        private TextView employeeEmailTextView;
        
        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            employeeNameTextView = itemView.findViewById(R.id.employeeNameTextView);
            employeeEmailTextView = itemView.findViewById(R.id.employeeEmailTextView);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEmployeeSelected(employees.get(position));
                }
            });
        }
        
        public void bind(Employee employee) {
            employeeNameTextView.setText(employee.getFullName());
            employeeEmailTextView.setText(employee.getEmail());
        }
    }
} 