package com.proyectofinal.frontend.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.proyectofinal.frontend.R;

public class WorkReportsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Por ahora, crear una vista simple con mensaje
        TextView textView = new TextView(getContext());
        textView.setText("📋 Módulo de Partes de Jornada\n\nEn desarrollo...\n\nAquí podrás registrar y consultar tus partes de trabajo diarios.");
        textView.setTextSize(16);
        textView.setPadding(32, 32, 32, 32);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setTextColor(getResources().getColor(R.color.colorOnSurface, null));
        
        return textView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Configuración adicional cuando esté implementado
    }
} 