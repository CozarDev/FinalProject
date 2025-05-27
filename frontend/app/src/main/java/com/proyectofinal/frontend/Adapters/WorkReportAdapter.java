package com.proyectofinal.frontend.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.proyectofinal.frontend.Models.WorkReport;
import com.proyectofinal.frontend.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WorkReportAdapter extends RecyclerView.Adapter<WorkReportAdapter.WorkReportViewHolder> {
    
    private List<WorkReport> workReports;
    private OnWorkReportClickListener listener;
    
    public interface OnWorkReportClickListener {
        void onWorkReportClick(WorkReport workReport);
    }
    
    public WorkReportAdapter(List<WorkReport> workReports, OnWorkReportClickListener listener) {
        this.workReports = workReports;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public WorkReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_work_report, parent, false);
        return new WorkReportViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WorkReportViewHolder holder, int position) {
        WorkReport workReport = workReports.get(position);
        holder.bind(workReport);
    }
    
    @Override
    public int getItemCount() {
        return workReports.size();
    }
    
    class WorkReportViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvEmployeeName;
        private TextView tvTimeRange;
        private TextView tvWorkedHours;
        private TextView tvBreakDuration;
        private TextView tvObservations;
        
        public WorkReportViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvDate = itemView.findViewById(R.id.tvReportDate);
            tvEmployeeName = itemView.findViewById(R.id.tvEmployeeName);
            tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
            tvWorkedHours = itemView.findViewById(R.id.tvWorkedHours);
            tvBreakDuration = itemView.findViewById(R.id.tvBreakDuration);
            tvObservations = itemView.findViewById(R.id.tvObservations);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onWorkReportClick(workReports.get(position));
                    }
                }
            });
        }
        
        public void bind(WorkReport workReport) {
            // Fecha del parte
            if (workReport.getReportDate() != null && !workReport.getReportDate().isEmpty()) {
                try {
                    // Convertir de String a LocalDate para formatear
                    LocalDate date = workReport.getReportDateAsLocalDate();
                    if (date != null) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        tvDate.setText(date.format(formatter));
                    } else {
                        tvDate.setText(workReport.getReportDate()); // Mostrar el string tal como viene
                    }
                } catch (Exception e) {
                    tvDate.setText(workReport.getReportDate()); // Mostrar el string tal como viene
                }
            } else {
                tvDate.setText("Fecha no disponible");
            }
            
            // Nombre del empleado (solo visible para jefes y admin)
            if (workReport.getEmployeeName() != null && !workReport.getEmployeeName().isEmpty()) {
                tvEmployeeName.setText("👤 " + workReport.getEmployeeName());
                tvEmployeeName.setVisibility(View.VISIBLE);
            } else {
                tvEmployeeName.setVisibility(View.GONE);
            }
            
            // Rango de horario
            String timeRange = String.format("🕐 %s - %s", 
                    workReport.getStartTime() != null ? workReport.getStartTime() : "N/A",
                    workReport.getEndTime() != null ? workReport.getEndTime() : "N/A");
            tvTimeRange.setText(timeRange);
            
            // Horas trabajadas
            tvWorkedHours.setText("⏱️ " + workReport.getWorkedHours());
            
            // Duración del descanso
            if (workReport.getBreakDuration() != null) {
                int hours = workReport.getBreakDuration() / 60;
                int minutes = workReport.getBreakDuration() % 60;
                String breakText = String.format("☕ %dh %02dm", hours, minutes);
                tvBreakDuration.setText(breakText);
            } else {
                tvBreakDuration.setText("☕ N/A");
            }
            
            // Observaciones
            if (workReport.getObservations() != null && !workReport.getObservations().trim().isEmpty()) {
                tvObservations.setText("📝 " + workReport.getObservations());
                tvObservations.setVisibility(View.VISIBLE);
            } else {
                tvObservations.setVisibility(View.GONE);
            }
        }
    }
} 