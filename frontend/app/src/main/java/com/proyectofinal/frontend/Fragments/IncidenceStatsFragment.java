package com.proyectofinal.frontend.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.proyectofinal.frontend.R;
import com.proyectofinal.frontend.Services.IncidenceApiService;
import com.proyectofinal.frontend.Api.ApiClient;

public class IncidenceStatsFragment extends Fragment {

    private static final String TAG = "IncidenceStatsFragment";

    private SwipeRefreshLayout swipeRefreshLayout;
    private CircularProgressIndicator progressIndicator;
    private ScrollView statsContainer;
    private LinearLayout emptyStateLayout;

    // Cards de estadísticas
    private CardView cardPending;
    private CardView cardInProgress;
    private CardView cardResolved;
    private CardView cardTotal;

    // TextViews para los números
    private TextView textPendingCount;
    private TextView textInProgressCount;
    private TextView textResolvedCount;
    private TextView textTotalCount;

    // TextViews para porcentajes
    private TextView textPendingPercent;
    private TextView textInProgressPercent;
    private TextView textResolvedPercent;

    private IncidenceApiService incidenceApiService;

    public static IncidenceStatsFragment newInstance() {
        return new IncidenceStatsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Inicializar API Service
        ApiClient apiClient = new ApiClient(requireContext());
        incidenceApiService = new IncidenceApiService(apiClient);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_incidence_stats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupSwipeRefresh();
        
        loadStats();
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        statsContainer = view.findViewById(R.id.statsContainer);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        // Cards
        cardPending = view.findViewById(R.id.cardPending);
        cardInProgress = view.findViewById(R.id.cardInProgress);
        cardResolved = view.findViewById(R.id.cardResolved);
        cardTotal = view.findViewById(R.id.cardTotal);

        // Números
        textPendingCount = view.findViewById(R.id.textPendingCount);
        textInProgressCount = view.findViewById(R.id.textInProgressCount);
        textResolvedCount = view.findViewById(R.id.textResolvedCount);
        textTotalCount = view.findViewById(R.id.textTotalCount);

        // Porcentajes
        textPendingPercent = view.findViewById(R.id.textPendingPercent);
        textInProgressPercent = view.findViewById(R.id.textInProgressPercent);
        textResolvedPercent = view.findViewById(R.id.textResolvedPercent);
    }

    private void setupSwipeRefresh() {
        // Deshabilitar pull-to-refresh individual 
        // La actualización se hace desde el botón principal que actualiza todas las pestañas
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
    }

    private void loadStats() {
        showLoading(true);
        
        incidenceApiService.getIncidenceStats(new IncidenceApiService.IncidenceStatsCallback() {
            @Override
            public void onSuccess(IncidenceApiService.IncidenceStats stats) {
                showLoading(false);
                displayStats(stats);
                showContent();
                Log.d(TAG, "Estadísticas cargadas: " + stats.toString());
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Log.e(TAG, "Error cargando estadísticas: " + error);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void displayStats(IncidenceApiService.IncidenceStats stats) {
        long total = stats.getTotal();
        
        // Mostrar números
        textPendingCount.setText(String.valueOf(stats.getPending()));
        textInProgressCount.setText(String.valueOf(stats.getInProgress()));
        textResolvedCount.setText(String.valueOf(stats.getResolved()));
        textTotalCount.setText(String.valueOf(total));

        // Calcular y mostrar porcentajes
        if (total > 0) {
            float pendingPercent = (stats.getPending() * 100.0f) / total;
            float inProgressPercent = (stats.getInProgress() * 100.0f) / total;
            float resolvedPercent = (stats.getResolved() * 100.0f) / total;

            textPendingPercent.setText(String.format("%.1f%%", pendingPercent));
            textInProgressPercent.setText(String.format("%.1f%%", inProgressPercent));
            textResolvedPercent.setText(String.format("%.1f%%", resolvedPercent));
        } else {
            textPendingPercent.setText("0%");
            textInProgressPercent.setText("0%");
            textResolvedPercent.setText("0%");
        }

        // Configurar colores de las cards
        setupCardColors();
    }

    private void setupCardColors() {
        // Estos colores deberían coincidir con los del adapter
        int pendingColor = 0xFFF39C12; // Naranja
        int inProgressColor = 0xFF3498DB; // Azul
        int resolvedColor = 0xFF27AE60; // Verde
        int totalColor = 0xFF9B59B6; // Púrpura

        cardPending.setCardBackgroundColor(pendingColor);
        cardInProgress.setCardBackgroundColor(inProgressColor);
        cardResolved.setCardBackgroundColor(resolvedColor);
        cardTotal.setCardBackgroundColor(totalColor);
    }

    private void showLoading(boolean show) {
        swipeRefreshLayout.setRefreshing(show);
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showContent() {
        statsContainer.setVisibility(View.VISIBLE);
        emptyStateLayout.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        statsContainer.setVisibility(View.GONE);
        emptyStateLayout.setVisibility(View.VISIBLE);
    }

    public void refreshData() {
        loadStats();
    }
} 