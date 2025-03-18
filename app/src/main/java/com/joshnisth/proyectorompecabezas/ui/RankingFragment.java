package com.joshnisth.proyectorompecabezas.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joshnisth.proyectorompecabezas.MainActivity;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;
import com.joshnisth.proyectorompecabezas.data.repositories.PuntuacionRepository;

import java.util.List;

public class RankingFragment extends Fragment {

    private PuntuacionRepository puntuacionRepository;
    private ListView listRanking;
    private TextView tvTitulo;
    private int tamanoSeleccionado = 3; // Por defecto 3x3

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ranking, container, false);
    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        puntuacionRepository = new PuntuacionRepository(requireContext());

        listRanking = view.findViewById(R.id.listRanking);
        tvTitulo = view.findViewById(R.id.tvTituloRanking);
        Button btnRanking3x3 = view.findViewById(R.id.btnRanking3x3);
        Button btnRanking4x4 = view.findViewById(R.id.btnRanking4x4);
        Button btnVolverMenu = view.findViewById(R.id.btnVolverMenu);
        Button btnResetRanking = view.findViewById(R.id.btnReiniciarRanking);

        // Carga inicial con tamaño por defecto
        cargarRanking(tamanoSeleccionado);

        // Cambiar a ranking 3x3
        btnRanking3x3.setOnClickListener(v -> {
            tamanoSeleccionado = 3;
            cargarRanking(tamanoSeleccionado);
        });

        // Cambiar a ranking 4x4
        btnRanking4x4.setOnClickListener(v -> {
            tamanoSeleccionado = 4;
            cargarRanking(tamanoSeleccionado);
        });

        // Volver al menú principal
        btnVolverMenu.setOnClickListener(v -> regresarAlMenu());

        // Reiniciar el ranking completo
        btnResetRanking.setOnClickListener(v -> {
            puntuacionRepository.eliminarRanking();
            cargarRanking(tamanoSeleccionado);
            Toast.makeText(getContext(), "Ranking reiniciado exitosamente", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Carga el ranking según el tamaño (3x3 o 4x4)
     */
    /**
     * Carga el ranking según el tamaño (3x3 o 4x4) de forma asíncrona
     */
    private void cargarRanking(int tamano) {
        tvTitulo.setText("🏆 Ranking " + tamano + "x" + tamano + " 🏆");
        // Usar método asíncrono
        puntuacionRepository.obtenerMejoresTiemposPorTamanoAsync(tamano, jugadores -> {
            requireActivity().runOnUiThread(() -> {
                if (jugadores.isEmpty()) {
                    // Lista vacía => un adapter con "No hay registros" o un text
                    listRanking.setAdapter(null);
                    Toast.makeText(getContext(), "No hay registros", Toast.LENGTH_SHORT).show();
                } else {
                    RankingAdapter adapter = new RankingAdapter(requireContext(), jugadores);
                    listRanking.setAdapter(adapter);
                }
            });
        });
    }



    /**
     * Formatea los segundos a formato mm:ss
     */
    private String formatearTiempo(int segundosTotales) {
        int minutos = segundosTotales / 60;
        int segundos = segundosTotales % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    /**
     * Muestra mensaje si no hay registros en la base de datos
     */
    private void mostrarMensajeSinRegistros() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, new String[]{"No hay registros"});
        listRanking.setAdapter(adapter);
    }

    /**
     * Acción para regresar al menú principal
     */
    private void regresarAlMenu() {
        ((MainActivity) requireActivity()).cargarFragment(new MenuFragment());
    }
}
