package com.proyectofinal.frontend.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyectofinal.frontend.Models.Department;
import com.proyectofinal.frontend.R;

import java.util.ArrayList;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.DepartmentViewHolder> {

    private List<Department> departments;
    private final OnDepartmentClickListener listener;

    // Interfaz para gestionar los eventos de clic
    public interface OnDepartmentClickListener {
        void onEditDepartment(Department department, int position);
        void onDeleteDepartment(Department department, int position);
    }

    // Constructor
    public DepartmentAdapter(OnDepartmentClickListener listener) {
        this.departments = new ArrayList<>();
        this.listener = listener;
    }

    // Método para establecer la lista de departamentos
    public void setDepartments(List<Department> departments) {
        this.departments = departments;
        notifyDataSetChanged();
    }

    // Método para añadir un departamento a la lista
    public void addDepartment(Department department) {
        this.departments.add(department);
        notifyItemInserted(this.departments.size() - 1);
    }

    // Método para actualizar un departamento existente
    public void updateDepartment(Department department, int position) {
        this.departments.set(position, department);
        notifyItemChanged(position);
    }

    // Método para eliminar un departamento de la lista
    public void removeDepartment(int position) {
        this.departments.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, this.departments.size());
    }

    @NonNull
    @Override
    public DepartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_department, parent, false);
        return new DepartmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DepartmentViewHolder holder, int position) {
        Department department = departments.get(position);
        holder.bind(department, position);
    }

    @Override
    public int getItemCount() {
        return departments.size();
    }

    // ViewHolder para los elementos de la lista
    class DepartmentViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView descriptionTextView;
        private final ImageButton editButton;
        private final ImageButton deleteButton;

        DepartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.departmentNameTextView);
            descriptionTextView = itemView.findViewById(R.id.departmentDescriptionTextView);
            editButton = itemView.findViewById(R.id.editDepartmentButton);
            deleteButton = itemView.findViewById(R.id.deleteDepartmentButton);
        }

        void bind(Department department, int position) {
            nameTextView.setText(department.getName());
            descriptionTextView.setText(department.getDescription());

            // Configurar listeners de clic
            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditDepartment(department, position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteDepartment(department, position);
                }
            });
        }
    }
} 