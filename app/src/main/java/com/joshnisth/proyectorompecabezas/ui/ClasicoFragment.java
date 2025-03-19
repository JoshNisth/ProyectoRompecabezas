package com.joshnisth.proyectorompecabezas.ui;

import android.app.AlertDialog;
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
import com.joshnisth.proyectorompecabezas.data.models.Rompecabezas;
import com.joshnisth.proyectorompecabezas.data.repositories.PuntuacionRepository;
import java.util.List;

public class ClasicoFragment extends Fragment {

    private PuntuacionRepository repo;
    private ListView listClásico;
    private RadioGroup rgTamano;
    private RadioButton radio3x3, radio4x4;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clasico, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = new PuntuacionRepository(requireContext());

        listClásico = view.findViewById(R.id.listClasico);
        rgTamano = view.findViewById(R.id.rgTamano);
        radio3x3 = view.findViewById(R.id.radio3x3);
        radio4x4 = view.findViewById(R.id.radio4x4);
        Button btnVolver = view.findViewById(R.id.btnVolverMenuClasico);

        // Cargar rompecabezas asíncronos
        cargarRompecabezas();

        // Al hacer click normal: abrir puzzle
        listClásico.setOnItemClickListener((parent, view1, position, id) -> {
            Rompecabezas seleccionado = (Rompecabezas) parent.getItemAtPosition(position);
            int tamano = (radio4x4.isChecked()) ? 4 : 3;

            // Abrir ArmarPuzzle con la ruta e nombre
            ArmarPuzzleFragment frag = ArmarPuzzleFragment.newInstance(
                    seleccionado.getRutaImagen(),
                    tamano,
                    seleccionado.getNombre()
            );
            ((MainActivity) requireActivity()).cargarFragment(frag);
        });

        // Al hacer click largo: confirmar eliminación
        listClásico.setOnItemLongClickListener((parent, view12, position, id) -> {
            Rompecabezas seleccionado = (Rompecabezas) parent.getItemAtPosition(position);
            mostrarDialogoEliminar(seleccionado);
            return true; // indica que manejamos el evento
        });

        btnVolver.setOnClickListener(v ->
                ((MainActivity) requireActivity()).cargarFragment(new MenuFragment())
        );
    }

    /**
     * Carga todos los rompecabezas asíncronamente
     */
    private void cargarRompecabezas() {
        repo.obtenerRompecabezasAsync(rompecabezas -> {
            requireActivity().runOnUiThread(() -> {
                if (rompecabezas.isEmpty()) {
                    Toast.makeText(getContext(), "No hay rompecabezas guardados", Toast.LENGTH_SHORT).show();
                }
                RompecabezasAdapter adapter = new RompecabezasAdapter(requireContext(), rompecabezas);
                listClásico.setAdapter(adapter);
            });
        });
    }

    /**
     * Muestra diálogo de confirmación para eliminar un rompecabezas
     */
    private void mostrarDialogoEliminar(Rompecabezas rompecabezas) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Puzzle")
                .setMessage("¿Deseas eliminar el rompecabezas '" + rompecabezas.getNombre() + "'?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Eliminar en repositorio
                    repo.eliminarRompecabezas(rompecabezas);
                    // Recargar lista
                    cargarRompecabezas();
                    Toast.makeText(getContext(), "Rompecabezas eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
