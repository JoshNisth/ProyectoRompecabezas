package com.joshnisth.proyectorompecabezas.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joshnisth.proyectorompecabezas.MainActivity;
import com.joshnisth.proyectorompecabezas.R;
import com.joshnisth.proyectorompecabezas.utils.PuzzleLogic; // Importamos la clase
import com.joshnisth.proyectorompecabezas.utils.PuzzleSolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ArmarPuzzleFragment extends Fragment {

    // Argumentos
    private static final String ARG_IMAGEN_URI = "imagen_uri";
    private static final String ARG_TAMANO     = "tamano";
    private static final String ARG_NOMBRE     = "nombre_puzzle";

    // UI
    private ImageView imagenReferencia;
    private GridLayout gridPuzzle;
    private TextView cronometro;
    private Button btnResolver;

    // Cronómetro
    private Handler handler;
    private int segundos = 0;
    private boolean corriendo = true;

    // Datos
    private int tamaño;
    private String nombrePuzzle;

    // Matrices
    private int[][] meta;         // estado final
    private int[][] posiciones;   // estado actual
    private Bitmap[] subBitmaps;  // piezas

    // Hueco actual (filaBlanca,colBlanca) - se rastrea al generar la UI
    private Bitmap bitmapOriginal;
    private int originalWidth, originalHeight;

    public static ArmarPuzzleFragment newInstance(String uriImagen, int tamano, String nombre) {
        ArmarPuzzleFragment frag = new ArmarPuzzleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGEN_URI, uriImagen);
        args.putInt(ARG_TAMANO, tamano);
        args.putString(ARG_NOMBRE, nombre);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_armar_puzzle, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imagenReferencia = view.findViewById(R.id.imagenReferencia);
        gridPuzzle       = view.findViewById(R.id.gridPuzzle);
        cronometro       = view.findViewById(R.id.cronometro);
        btnResolver      = view.findViewById(R.id.btnResolver);

        if (getArguments() != null) {
            String uriImagen = getArguments().getString(ARG_IMAGEN_URI);
            tamaño          = getArguments().getInt(ARG_TAMANO);
            nombrePuzzle    = getArguments().getString(ARG_NOMBRE);

            try {
                // Cargar imagen original
                InputStream is = requireActivity().getContentResolver().openInputStream(Uri.parse(uriImagen));
                bitmapOriginal = BitmapFactory.decodeStream(is);
                originalWidth  = bitmapOriginal.getWidth();
                originalHeight = bitmapOriginal.getHeight();

                // Escalar a 300x300 para generar subBitmaps
                Bitmap bitmapPuzzle = Bitmap.createScaledBitmap(
                        bitmapOriginal,
                        PuzzleLogic.PUZZLE_SIZE,
                        PuzzleLogic.PUZZLE_SIZE,
                        true
                );

                // Dividir en subBitmaps
                subBitmaps = PuzzleLogic.dividirImagen(bitmapPuzzle, tamaño);

                // Generar meta (estado final)
                meta = PuzzleLogic.generarEstadoFinal(tamaño);

                // Pintar hueco en referencia (tarea tuya con pintarBlancoEnReferencia)
                // Ejemplo:
                Bitmap refConHueco = pintarBlancoEnReferencia(/* meta y demas */);
                imagenReferencia.setImageBitmap(refConHueco);

                // Copiar meta a posiciones y mezclar
                posiciones = PuzzleLogic.copiarEstadoFinal(meta);
                posiciones = PuzzleLogic.mezclarPuzzle(posiciones);

                // Mostrar puzzle
                actualizarUI();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        iniciarCronometro();

        btnResolver.setOnClickListener(v -> {
            btnResolver.setEnabled(false); // Bloquear botón tras presionarlo
            corriendo = false;
            handler.removeCallbacksAndMessages(null);

            // Bloquear interacción con el puzzle
            for (int i = 0; i < gridPuzzle.getChildCount(); i++) {
                gridPuzzle.getChildAt(i).setOnClickListener(null);
            }

            new Thread(() -> {
                PuzzleSolver solver = new PuzzleSolver();
                List<int[][]> solucion = solver.solve(posiciones, meta, tamaño);

                if (solucion == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "No se encontró solución", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // Reproducir pasos de la solución
                for (int[][] estado : solucion) {
                    requireActivity().runOnUiThread(() -> actualizarUIConEstado(estado));
                    try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                }

                // Mostrar FinJuegoFragment bloqueado hasta que el usuario seleccione una opción
                requireActivity().runOnUiThread(() -> {
                    FinJuegoFragment dialog = FinJuegoFragment.newInstance("00:00", true, tamaño);
                    dialog.setCancelable(false); // Bloquear el cierre manual del diálogo
                    dialog.show(getParentFragmentManager(), "FinJuegoFragment");
                });

            }).start();
        });
        Button btnVolver = view.findViewById(R.id.btnRegresarArmarMenu);
        btnVolver.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).cargarFragment(new MenuFragment());
        });



    }
    private void actualizarUIConEstado(int[][] estado) {
        // Copiar 'estado' en 'posiciones'
        for (int i=0; i<tamaño; i++){
            for (int j=0; j<tamaño; j++){
                posiciones[i][j] = estado[i][j];
            }
        }
        // Llamar actualizarUI()
        actualizarUI();
    }

    /**
     * Pinta en blanco en la imagen original la posición
     * del hueco definido en meta[][].
     * Devuelve un Bitmap mutable con el parche blanco.
     */
    private Bitmap pintarBlancoEnReferencia() {
        // 1) Creamos copia mutable de la imagen original (bitmapOriginal)
        Bitmap mutableRef = bitmapOriginal.copy(Bitmap.Config.ARGB_8888, true);

        // 2) Buscar hueco en meta[][]  (filaHueco, colHueco)
        int filaHueco = -1, colHueco = -1;
        for (int f = 0; f < tamaño; f++) {
            for (int c = 0; c < tamaño; c++) {
                if (meta[f][c] == -1) {
                    filaHueco = f;
                    colHueco = c;
                    break;
                }
            }
            if (filaHueco != -1) break;
        }

        // 3) Factor de escala entre puzzle (300x300) y la imagen original
        float ratioX = (float) originalWidth / PuzzleLogic.PUZZLE_SIZE;   // e.g. wOriginal / 300
        float ratioY = (float) originalHeight / PuzzleLogic.PUZZLE_SIZE; // e.g. hOriginal / 300

        // 4) Tamaño de cada pieza en el puzzle escalado (300x300)
        int anchoPieza = PuzzleLogic.PUZZLE_SIZE / tamaño;
        int altoPieza  = PuzzleLogic.PUZZLE_SIZE / tamaño;

        // 5) Coordenadas del hueco en la versión puzzle(300x300)
        int x0Puzzle = colHueco * anchoPieza;
        int y0Puzzle = filaHueco * altoPieza;

        // 6) Convertir esas coords a la imagen original
        int x0Ref    = (int) (x0Puzzle  * ratioX);
        int y0Ref    = (int) (y0Puzzle  * ratioY);
        int anchoRef = (int) (anchoPieza* ratioX);
        int altoRef  = (int) (altoPieza * ratioY);

        // 7) Pintar en blanco el parche
        for (int y = y0Ref; y < y0Ref + altoRef; y++) {
            for (int x = x0Ref; x < x0Ref + anchoRef; x++) {
                // Verificamos que x,y estén dentro de los límites
                if (x >= 0 && x < mutableRef.getWidth() &&
                        y >= 0 && y < mutableRef.getHeight()) {
                    mutableRef.setPixel(x, y, Color.WHITE);
                }
            }
        }

        // 8) Retornamos el Bitmap con el parche
        return mutableRef;
    }


    private void actualizarUI() {
        gridPuzzle.removeAllViews();
        gridPuzzle.setColumnCount(tamaño);
        gridPuzzle.setRowCount(tamaño);

        for (int fila = 0; fila < tamaño; fila++) {
            for (int col = 0; col < tamaño; col++) {
                ImageView pieza = new ImageView(getContext());
                int val = posiciones[fila][col];

                if (val == -1) {
                    pieza.setBackgroundColor(Color.WHITE);
                } else {
                    pieza.setImageBitmap(subBitmaps[val]);
                    pieza.setOnClickListener(this::moverPieza);
                }

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(fila, 1f),
                        GridLayout.spec(col, 1f)
                );
                params.width = 0;
                params.height= 0;
                pieza.setLayoutParams(params);

                gridPuzzle.addView(pieza);
            }
        }
        gridPuzzle.invalidate();
        gridPuzzle.requestLayout();
    }

    private void moverPieza(View view) {
        int pos = gridPuzzle.indexOfChild(view);
        int filaAct = pos / tamaño;
        int colAct  = pos % tamaño;

        // Buscar hueco actual
        int filaHueco=-1, colHueco=-1;
        outer:
        for (int f=0; f<tamaño; f++){
            for (int c=0; c<tamaño; c++){
                if (posiciones[f][c] == -1) {
                    filaHueco=f; colHueco=c;
                    break outer;
                }
            }
        }

        boolean esMovible =
                (filaAct == filaHueco && Math.abs(colAct - colHueco) == 1) ||
                        (colAct == colHueco && Math.abs(filaAct - filaHueco) == 1);

        if (esMovible) {
            int temp = posiciones[filaAct][colAct];
            posiciones[filaAct][colAct] = -1;
            posiciones[filaHueco][colHueco] = temp;

            actualizarUI();
            verificarVictoria();
        }
    }

    private void verificarVictoria() {
        // Simplemente usar la lógica
        boolean resuelto = PuzzleLogic.estaResuelto(posiciones, meta);
        if (resuelto) {
            corriendo = false;
            handler.removeCallbacksAndMessages(null);

            // Llamar al FinJuegoFragment
            String tiempoFinal = cronometro.getText().toString();
            boolean esAutomatico = false;
            FinJuegoFragment dialog = FinJuegoFragment.newInstance(tiempoFinal, esAutomatico, tamaño);
            dialog.show(getParentFragmentManager(), "FinJuegoFragment");
        }
    }

    private void iniciarCronometro() {
        handler = new Handler();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (corriendo) {
                    segundos++;
                    int minutos = segundos / 60;
                    int seg = segundos % 60;
                    cronometro.setText(String.format("%02d:%02d", minutos, seg));
                    handler.postDelayed(this, 1000);
                }
            }
        };
        handler.post(r);
    }
}
