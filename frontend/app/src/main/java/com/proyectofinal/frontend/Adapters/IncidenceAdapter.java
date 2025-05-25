package com.proyectofinal.frontend.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.proyectofinal.frontend.Models.Incidence;
import com.proyectofinal.frontend.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IncidenceAdapter extends RecyclerView.Adapter<IncidenceAdapter.IncidenceViewHolder> {

    private List<Incidence> incidences;
    private final Context context;
    private final OnIncidenceActionListener listener;
    private final UserRole userRole;
    private final boolean showActions;
    private final SimpleDateFormat dateFormat;

    // Enum para roles de usuario
    public enum UserRole {
        ADMIN,
        INCIDENCE_EMPLOYEE,
        INCIDENCE_MANAGER,
        DEPARTMENT_HEAD,
        EMPLOYEE
    }

    // Interface para callbacks de acciones
    public interface OnIncidenceActionListener {
        void onIncidenceClick(Incidence incidence);
        void onAcceptIncidence(Incidence incidence);
        void onResolveIncidence(Incidence incidence);
        void onDeleteIncidence(Incidence incidence);
    }

    public IncidenceAdapter(Context context, UserRole userRole, boolean showActions, 
                           OnIncidenceActionListener listener) {
        this.context = context;
        this.incidences = new ArrayList<>();
        this.userRole = userRole;
        this.showActions = showActions;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public IncidenceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_incidence, parent, false);
        return new IncidenceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenceViewHolder holder, int position) {
        Incidence incidence = incidences.get(position);
        holder.bind(incidence);
    }

    @Override
    public int getItemCount() {
        return incidences.size();
    }

    public void updateIncidences(List<Incidence> newIncidences) {
        this.incidences.clear();
        if (newIncidences != null) {
            this.incidences.addAll(newIncidences);
        }
        notifyDataSetChanged();
    }

    public void addIncidence(Incidence incidence) {
        this.incidences.add(0, incidence);
        notifyItemInserted(0);
    }

    public void removeIncidence(Incidence incidence) {
        int position = incidences.indexOf(incidence);
        if (position != -1) {
            incidences.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateIncidence(Incidence updatedIncidence) {
        for (int i = 0; i < incidences.size(); i++) {
            if (incidences.get(i).getId().equals(updatedIncidence.getId())) {
                incidences.set(i, updatedIncidence);
                notifyItemChanged(i);
                break;
            }
        }
    }

    class IncidenceViewHolder extends RecyclerView.ViewHolder {

        private final TextView priorityEmoji;
        private final TextView incidenceTitle;
        private final TextView incidenceDescription;
        private final TextView incidenceStatus;
        private final TextView priorityText;
        private final TextView createdDate;
        private final TextView createdByInfo;
        private final TextView assignedToInfo;
        private final View priorityIndicator;
        private final LinearLayout additionalInfo;
        private final LinearLayout actionButtons;
        private final LinearLayout urgencyIndicator;
        private final Button btnAccept;
        private final Button btnResolve;
        private final Button btnDelete;

        public IncidenceViewHolder(@NonNull View itemView) {
            super(itemView);

            priorityEmoji = itemView.findViewById(R.id.priorityEmoji);
            incidenceTitle = itemView.findViewById(R.id.incidenceTitle);
            incidenceDescription = itemView.findViewById(R.id.incidenceDescription);
            incidenceStatus = itemView.findViewById(R.id.incidenceStatus);
            priorityText = itemView.findViewById(R.id.priorityText);
            createdDate = itemView.findViewById(R.id.createdDate);
            createdByInfo = itemView.findViewById(R.id.createdByInfo);
            assignedToInfo = itemView.findViewById(R.id.assignedToInfo);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);
            additionalInfo = itemView.findViewById(R.id.additionalInfo);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            urgencyIndicator = itemView.findViewById(R.id.urgencyIndicator);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnResolve = itemView.findViewById(R.id.btnResolve);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            // Click en el item completo
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncidenceClick(incidences.get(getAdapterPosition()));
                }
            });

            // Click en botones de acción
            btnAccept.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptIncidence(incidences.get(getAdapterPosition()));
                }
            });

            btnResolve.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onResolveIncidence(incidences.get(getAdapterPosition()));
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteIncidence(incidences.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Incidence incidence) {
            // Información básica
            incidenceTitle.setText(incidence.getTitle());
            incidenceDescription.setText(incidence.getDescription());

            // Prioridad
            priorityEmoji.setText(incidence.getPriorityEmoji());
            priorityText.setText(incidence.getPriorityText());
            
            // Estado
            incidenceStatus.setText(incidence.getStatusEmoji() + " " + incidence.getStatusText());

            // Fecha
            if (incidence.getCreatedAt() != null) {
                String timeAgo = getTimeAgo(incidence);
                createdDate.setText(timeAgo);
            }

            // Colores según prioridad y estado
            setupColors(incidence);

            // Información adicional (según rol)
            setupAdditionalInfo(incidence);

            // Indicador de urgencia
            setupUrgencyIndicator(incidence);

            // Botones de acción (según rol y estado)
            setupActionButtons(incidence);
        }

        private void setupColors(Incidence incidence) {
            // Color del indicador de prioridad
            int priorityColor = incidence.getPriorityColor();
            priorityIndicator.setBackgroundColor(priorityColor);

            // Fondo del texto de prioridad
            GradientDrawable priorityBg = new GradientDrawable();
            priorityBg.setShape(GradientDrawable.RECTANGLE);
            priorityBg.setCornerRadius(16f);
            priorityBg.setColor(priorityColor);
            priorityText.setBackground(priorityBg);

            // Fondo del texto de estado
            int statusColor = incidence.getStatusColor();
            GradientDrawable statusBg = new GradientDrawable();
            statusBg.setShape(GradientDrawable.RECTANGLE);
            statusBg.setCornerRadius(16f);
            statusBg.setColor(statusColor);
            incidenceStatus.setBackground(statusBg);
        }

        private void setupAdditionalInfo(Incidence incidence) {
            boolean showAdditionalInfo = userRole == UserRole.ADMIN || 
                                       userRole == UserRole.INCIDENCE_MANAGER ||
                                       userRole == UserRole.INCIDENCE_EMPLOYEE ||
                                       userRole == UserRole.DEPARTMENT_HEAD;

            if (showAdditionalInfo) {
                additionalInfo.setVisibility(View.VISIBLE);
                
                // TODO: Aquí necesitarías obtener los nombres reales de los empleados
                // Por ahora mostramos los IDs
                createdByInfo.setText("👤 Creado por: " + 
                    (incidence.getCreatedBy() != null ? incidence.getCreatedBy() : "Desconocido"));

                if (incidence.getAssignedTo() != null && !incidence.getAssignedTo().isEmpty()) {
                    assignedToInfo.setVisibility(View.VISIBLE);
                    assignedToInfo.setText("🔧 Asignado a: " + incidence.getAssignedTo());
                } else {
                    assignedToInfo.setVisibility(View.GONE);
                }
            } else {
                additionalInfo.setVisibility(View.GONE);
            }
        }

        private void setupUrgencyIndicator(Incidence incidence) {
            if (incidence.isUrgent()) {
                urgencyIndicator.setVisibility(View.VISIBLE);
            } else {
                urgencyIndicator.setVisibility(View.GONE);
            }
        }

        private void setupActionButtons(Incidence incidence) {
            if (!showActions) {
                actionButtons.setVisibility(View.GONE);
                return;
            }

            // Resetear visibilidad de botones
            btnAccept.setVisibility(View.GONE);
            btnResolve.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            boolean showAnyButton = false;

            switch (userRole) {
                case ADMIN:
                    // ADMIN: Puede crear y eliminar cualquier incidencia
                    btnDelete.setVisibility(View.VISIBLE);
                    showAnyButton = true;
                    break;

                case INCIDENCE_MANAGER:
                    // Jefe departamento Incidencias: Todo lo anterior + ver activas, en curso con asignaciones, y resueltas
                    // Mismo comportamiento que empleados de incidencias para las acciones
                    if (incidence.canBeAccepted()) {
                        btnAccept.setVisibility(View.VISIBLE);
                        showAnyButton = true;
                    }
                    if (incidence.canBeResolved()) {
                        btnResolve.setVisibility(View.VISIBLE);
                        showAnyButton = true;
                    }
                    break;

                case INCIDENCE_EMPLOYEE:
                    // Empleados departamento Incidencias: Ver pendientes ordenadas por prioridad, 
                    // aceptar incidencias (PENDIENTE→EN_CURSO), resolver asignadas (EN_CURSO→SOLVENTADA)
                    if (incidence.canBeAccepted()) {
                        btnAccept.setVisibility(View.VISIBLE);
                        showAnyButton = true;
                    }
                    if (incidence.canBeResolved()) {
                        btnResolve.setVisibility(View.VISIBLE);
                        showAnyButton = true;
                    }
                    break;

                case DEPARTMENT_HEAD:
                    // Jefes otros departamentos: Crear incidencias, ver suyas y de empleados de su departamento, 
                    // eliminar pendientes
                    if (incidence.canBeDeleted()) {
                        btnDelete.setVisibility(View.VISIBLE);
                        showAnyButton = true;
                    }
                    break;

                case EMPLOYEE:
                    // Empleados otros departamentos: Crear propias (automáticamente PENDIENTE), 
                    // ver solo suyas, eliminar solo pendientes propias
                    if (incidence.canBeDeleted()) {
                        btnDelete.setVisibility(View.VISIBLE);
                        showAnyButton = true;
                    }
                    break;
            }

            actionButtons.setVisibility(showAnyButton ? View.VISIBLE : View.GONE);
        }

        private String getTimeAgo(Incidence incidence) {
            if (incidence.getCreatedAt() == null) {
                return "Fecha desconocida";
            }

            long diffInMillis = System.currentTimeMillis() - incidence.getCreatedAt().getTime();
            long diffInHours = diffInMillis / (1000 * 60 * 60);
            long diffInDays = diffInHours / 24;

            if (diffInDays > 0) {
                return "Hace " + diffInDays + " día" + (diffInDays > 1 ? "s" : "");
            } else if (diffInHours > 0) {
                return "Hace " + diffInHours + " hora" + (diffInHours > 1 ? "s" : "");
            } else {
                long diffInMinutes = diffInMillis / (1000 * 60);
                if (diffInMinutes > 0) {
                    return "Hace " + diffInMinutes + " minuto" + (diffInMinutes > 1 ? "s" : "");
                } else {
                    return "Ahora";
                }
            }
        }
    }
} 