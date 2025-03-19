package com.joshnisth.proyectorompecabezas.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joshnisth.proyectorompecabezas.MainActivity;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.utils.MusicManager;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_configuraciones, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Switch switchMusica = view.findViewById(R.id.switchMusica);
        Button btnCambiarCancion = view.findViewById(R.id.btnCambiarCancion);
        Button btnVolver = view.findViewById(R.id.btnRegresarMenuSettings);

        // Inicializar estado del Switch según isMusicOn
        switchMusica.setChecked(MusicManager.isMusicOn());

        // Activar / Desactivar música al cambiar el switch
        switchMusica.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MusicManager.setMusicOn(isChecked);
            if (isChecked) {
                MusicManager.startMusic(requireContext());
            } else {
                MusicManager.stopMusic();
            }
        });

        // Botón cambiar canción
        btnCambiarCancion.setOnClickListener(v -> {
            if (MusicManager.isMusicOn()) {
                MusicManager.changeSongRandom(requireContext());
            }
        });

        // Botón regresar al menú principal
        btnVolver.setOnClickListener(v ->
                ((MainActivity) requireActivity()).cargarFragment(new MenuFragment())
        );
    }
}
