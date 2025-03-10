package com.joshnisth.proyectorompecabezas.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.joshnisth.proyectorompecabezas.MainActivity;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;
import com.joshnisth.proyectorompecabezas.data.repositories.PuntuacionRepository;

public class FinJuegoFragment extends DialogFragment {

    private static final String ARG_TIEMPO = "tiempo";
    private static final String ARG_AUTOMATICO = "automatico";

    private String tiempo;
    private boolean esAutomatico;
    private PuntuacionRepository puntuacionRepository;

    public static FinJuegoFragment newInstance(String tiempo, boolean esAutomatico) {
        FinJuegoFragment fragment = new FinJuegoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TIEMPO, tiempo);
        args.putBoolean(ARG_AUTOMATICO, esAutomatico);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        puntuacionRepository = new PuntuacionRepository(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fin_juego, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvMensaje = view.findViewById(R.id.tvMensaje);
        TextView tvTiempo = view.findViewById(R.id.tvTiempo);
        EditText etNombre = view.findViewById(R.id.etNombre);
        Button btnGuardar = view.findViewById(R.id.btnGuardar);
        Button btnSalir = view.findViewById(R.id.btnSalir);

        if (getArguments() != null) {
            tiempo = getArguments().getString(ARG_TIEMPO);
            esAutomatico = getArguments().getBoolean(ARG_AUTOMATICO);
        }

        if (esAutomatico) {
            tvMensaje.setText("El puzzle se resolvió automáticamente. Resuelvelo manualmente para poder registrar tu tiempo.");
            tvTiempo.setVisibility(View.GONE);
            etNombre.setVisibility(View.GONE);
            btnGuardar.setVisibility(View.GONE);
        } else {
            tvMensaje.setText("¡FELICIDADES! RESOLVISTE EL PUZZLE EN:");
            tvTiempo.setText(tiempo);
        }

        btnGuardar.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(getContext(), "Ingresa tu nombre", Toast.LENGTH_SHORT).show();
            } else {
                puntuacionRepository.insertarJugador(new Jugador(nombre, convertirTiempoASegundos(tiempo)));
                Toast.makeText(getContext(), "Guardado en el ranking", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });

        btnSalir.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            getActivity().finish();
        });
    }

    private int convertirTiempoASegundos(String tiempo) {
        String[] partes = tiempo.split(":");
        int minutos = Integer.parseInt(partes[0]);
        int segundos = Integer.parseInt(partes[1]);
        return (minutos * 60) + segundos;
    }
}
