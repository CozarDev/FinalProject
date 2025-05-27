package com.proyectofinal.frontend.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.proyectofinal.frontend.Api.ApiClient;
import com.proyectofinal.frontend.Models.Incidence;
import com.proyectofinal.frontend.R;
import com.proyectofinal.frontend.Services.IncidenceApiService;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class IncidenceDetailFragment extends Fragment {

    private static final String TAG = "IncidenceDetailFragment";
    private static final String ARG_INCIDENCE_ID = "incidence_id";

    private String incidenceId;
    private Incidence currentIncidence;
    private IncidenceApiService incidenceApiService;
    private String currentUserId;

    // Views
    private TextView tvTitle, tvDescription, tvPriority, tvStatus, tvCreatedBy, tvCreatedAt, tvAssignedTo;
    private Button btnResolve, btnBack;
    private MaterialCardView cardIncidence;

    // Listener para notificar cuando se resuelve la incidencia
    public interface OnIncidenceResolvedListener {
        void onIncidenceResolved();
    }

    private OnIncidenceResolvedListener resolvedListener;

    public static IncidenceDetailFragment newInstance(String incidenceId) {
        IncidenceDetailFragment fragment = new IncidenceDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INCIDENCE_ID, incidenceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            incidenceId = getArguments().getString(ARG_INCIDENCE_ID);
        }

        // Obtener información del usuario
        SharedPreferences sharedPrefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentUserId = sharedPrefs.getString("USER_ID", "");

        // Inicializar API Service
        ApiClient apiClient = new ApiClient(requireContext());
        incidenceApiService = new IncidenceApiService(apiClient);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_incidence_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
        loadIncidenceDetails();
    }

    private void initializeViews(View view) {
        cardIncidence = view.findViewById(R.id.cardIncidence);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvPriority = view.findViewById(R.id.tvPriority);
        tvStatus = view.findViewById(R.id.tvStatus);
        tvCreatedBy = view.findViewById(R.id.tvCreatedBy);
        tvCreatedAt = view.findViewById(R.id.tvCreatedAt);
        tvAssignedTo = view.findViewById(R.id.tvAssignedTo);
        btnResolve = view.findViewById(R.id.btnResolve);
        btnBack = view.findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnResolve.setOnClickListener(v -> showResolveDialog());
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void loadIncidenceDetails() {
        if (incidenceId == null) {
            Toast.makeText(requireContext(), "Error: ID de incidencia no válido", Toast.LENGTH_SHORT).show();
            return;
        }

        incidenceApiService.getIncidenceById(incidenceId, new IncidenceApiService.IncidenceCallback() {
            @Override
            public void onSuccess(Incidence incidence) {
                currentIncidence = incidence;
                displayIncidenceDetails(incidence);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error cargando detalles de incidencia: " + error);
                Toast.makeText(requireContext(), "Error al cargar detalles: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayIncidenceDetails(Incidence incidence) {
        tvTitle.setText(incidence.getTitle());
        tvDescription.setText(incidence.getDescription());
        
        // Configurar prioridad con color
        tvPriority.setText(getPriorityText(incidence.getPriority()));
        tvPriority.setTextColor(getPriorityColor(incidence.getPriority()));
        
        // Configurar estado
        tvStatus.setText(getStatusText(incidence.getStatus()));
        
        tvCreatedBy.setText("Creado por: " + incidence.getCreatedBy());
        
        // Formatear fecha
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        tvCreatedAt.setText("Fecha: " + dateFormat.format(incidence.getCreatedAt()));
        
        tvAssignedTo.setText("Asignado a: " + (incidence.getAssignedTo() != null ? incidence.getAssignedTo() : "Sin asignar"));
        
        // Mostrar botón de resolver solo si está en progreso y asignado al usuario actual
        boolean canResolve = incidence.getStatus() == Incidence.Status.EN_CURSO && 
                           currentUserId.equals(incidence.getAssignedTo());
        btnResolve.setVisibility(canResolve ? View.VISIBLE : View.GONE);
    }

    private String getPriorityText(Incidence.Priority priority) {
        switch (priority) {
            case ALTA: return "🔴 ALTA";
            case MEDIA: return "🟡 MEDIA";
            case BAJA: return "🟢 BAJA";
            default: return "❓ DESCONOCIDA";
        }
    }

    private int getPriorityColor(Incidence.Priority priority) {
        switch (priority) {
            case ALTA: return getResources().getColor(android.R.color.holo_red_dark, null);
            case MEDIA: return getResources().getColor(android.R.color.holo_orange_dark, null);
            case BAJA: return getResources().getColor(android.R.color.holo_green_dark, null);
            default: return getResources().getColor(android.R.color.black, null);
        }
    }

    private String getStatusText(Incidence.Status status) {
        switch (status) {
            case PENDIENTE: return "⏳ PENDIENTE";
            case EN_CURSO: return "🔧 EN CURSO";
            case SOLVENTADA: return "✅ SOLVENTADA";
            default: return "❓ DESCONOCIDO";
        }
    }

    private void showResolveDialog() {
        if (currentIncidence == null) return;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Resolver Incidencia")
                .setMessage("¿Estás seguro de que quieres marcar esta incidencia como resuelta?\n\n" + 
                           currentIncidence.getTitle() + "\n\nEsta acción no se puede deshacer.")
                .setPositiveButton("Resolver", (dialog, which) -> resolveIncidence())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void resolveIncidence() {
        if (currentIncidence == null) return;

        incidenceApiService.resolveIncidence(currentIncidence.getId(), new IncidenceApiService.IncidenceCallback() {
            @Override
            public void onSuccess(Incidence updatedIncidence) {
                Toast.makeText(requireContext(), "Incidencia resuelta exitosamente", Toast.LENGTH_SHORT).show();
                
                // Notificar al listener que se resolvió la incidencia
                if (resolvedListener != null) {
                    resolvedListener.onIncidenceResolved();
                }
                
                // Volver atrás
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), "Error al resolver: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setOnIncidenceResolvedListener(OnIncidenceResolvedListener listener) {
        this.resolvedListener = listener;
    }
} 