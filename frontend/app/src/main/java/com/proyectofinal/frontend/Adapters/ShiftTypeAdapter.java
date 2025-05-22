package com.proyectofinal.frontend.Adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyectofinal.frontend.Models.ShiftType;
import com.proyectofinal.frontend.R;

import java.util.ArrayList;
import java.util.List;

public class ShiftTypeAdapter extends RecyclerView.Adapter<ShiftTypeAdapter.ViewHolder> {

    private List<ShiftType> shiftTypes;
    private OnShiftTypeClickListener listener;

    public ShiftTypeAdapter(OnShiftTypeClickListener listener) {
        this.shiftTypes = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shift_type, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShiftType shiftType = shiftTypes.get(position);
        holder.bind(shiftType);
    }

    @Override
    public int getItemCount() {
        return shiftTypes.size();
    }

    public void setShiftTypes(List<ShiftType> shiftTypes) {
        this.shiftTypes = shiftTypes;
        notifyDataSetChanged();
    }

    public void addShiftType(ShiftType shiftType) {
        shiftTypes.add(shiftType);
        notifyItemInserted(shiftTypes.size() - 1);
    }

    public void updateShiftType(ShiftType shiftType) {
        for (int i = 0; i < shiftTypes.size(); i++) {
            if (shiftTypes.get(i).getId().equals(shiftType.getId())) {
                shiftTypes.set(i, shiftType);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeShiftType(String shiftTypeId) {
        for (int i = 0; i < shiftTypes.size(); i++) {
            if (shiftTypes.get(i).getId().equals(shiftTypeId)) {
                shiftTypes.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public interface OnShiftTypeClickListener {
        void onEditShiftType(ShiftType shiftType, int position);
        void onDeleteShiftType(ShiftType shiftType, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View colorIndicator;
        private TextView nameTextView;
        private TextView timeTextView;
        private TextView daysTextView;
        private ImageButton editButton;
        private ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            daysTextView = itemView.findViewById(R.id.daysTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);

            editButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditShiftType(shiftTypes.get(position), position);
                }
            });

            deleteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeleteShiftType(shiftTypes.get(position), position);
                }
            });
        }

        public void bind(ShiftType shiftType) {
            nameTextView.setText(shiftType.getName());
            timeTextView.setText(shiftType.getStartTime() + " - " + shiftType.getEndTime());
            daysTextView.setText(shiftType.getWorkDaysAsString());
            
            // Establecer el color del indicador
            try {
                colorIndicator.setBackgroundColor(Color.parseColor(shiftType.getColor()));
            } catch (Exception e) {
                // Si el color no es válido, usar un color predeterminado
                colorIndicator.setBackgroundColor(Color.BLUE);
            }
        }
    }
} 