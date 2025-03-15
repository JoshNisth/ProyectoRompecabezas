package com.joshnisth.proyectorompecabezas.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.joshnisth.proyectorompecabezas.MainActivity;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.ui.CrearPuzzleFragment;

public class MenuFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnPersonalizado = view.findViewById(R.id.btnPersonalizado);
        ImageButton btnClasico = view.findViewById(R.id.btnClasico);
        ImageButton btnClasificatoria = view.findViewById(R.id.btnClasificatoria);
        ImageButton btnConfiguraciones = view.findViewById(R.id.btnConfiguraciones);
        Button btnSalir = view.findViewById(R.id.btnSalir);

        btnPersonalizado.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).cargarFragment(new CrearPuzzleFragment());
        });

        btnClasico.setOnClickListener(v -> {
            // Aquí puedes cargar el fragmento de modo clásico si lo necesitas
        });

        btnClasificatoria.setOnClickListener(v -> {
            // Aquí puedes cargar el fragmento de modo clasificatoria si lo necesitas
        });

        btnConfiguraciones.setOnClickListener(v -> {
            // Aquí puedes cargar el fragmento de configuraciones si lo necesitas
        });

        btnSalir.setOnClickListener(v -> {
            requireActivity().finish();
        });
    }
}
