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

import com.joshnisth.proyectorompecabezas.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

public class ArmarPuzzleFragment extends Fragment {

    // Argumentos para construir este Fragment
    private static final String ARG_IMAGEN_URI = "imagen_uri";
    private static final String ARG_TAMANO     = "tamano";
    private static final String ARG_NOMBRE     = "nombre_puzzle";

    // Referencias de la UI
    private ImageView imagenReferencia;
    private GridLayout gridPuzzle;
    private TextView cronometro, tvNombrePuzzle;
    private Button btnResolver;

    // Cronómetro
    private Handler handler;
    private int segundos = 0;
    private boolean corriendo = true;

    // Parámetros del puzzle
    private static final int PUZZLE_SIZE = 300;  // Para subBitmaps
    private int tamaño;  // Puede ser 3 o 4
    private String nombrePuzzle;

    // Matrices y piezas
    private int[][] meta;        // Estado final deseado
    private int[][] posiciones;  // Estado actual
    private Bitmap[] subBitmaps; // Subimágenes (nunca null)
    private int filaBlanca, colBlanca; // Posición actual de la pieza en blanco

    // Imagen original (sin escalar)
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

        // 1) Referencias a la UI
        imagenReferencia = view.findViewById(R.id.imagenReferencia);
        gridPuzzle       = view.findViewById(R.id.gridPuzzle);
        cronometro       = view.findViewById(R.id.cronometro);
        tvNombrePuzzle   = view.findViewById(R.id.tvNombrePuzzle);
        btnResolver      = view.findViewById(R.id.btnResolver);

        // 2) Recuperar argumentos
        if (getArguments() != null) {
            String uriImagen = getArguments().getString(ARG_IMAGEN_URI);
            tamaño          = getArguments().getInt(ARG_TAMANO);
            nombrePuzzle    = getArguments().getString(ARG_NOMBRE);

            tvNombrePuzzle.setText(nombrePuzzle);

            try {
                // 3) Cargar imagen original
                InputStream is = requireActivity().getContentResolver().openInputStream(Uri.parse(uriImagen));
                bitmapOriginal = BitmapFactory.decodeStream(is);
                originalWidth  = bitmapOriginal.getWidth();
                originalHeight = bitmapOriginal.getHeight();

                // 4) Escalar a 300x300 para generar subBitmaps
                Bitmap bitmapPuzzle = Bitmap.createScaledBitmap(bitmapOriginal, PUZZLE_SIZE, PUZZLE_SIZE, true);

                // 5) Dividir en subBitmaps
                dividirImagen(bitmapPuzzle);

                // 6) Generar la matriz final (meta[][]) con hueco aleatorio
                generarEstadoFinalConHuecoAleatorio();

                // 7) Pintar el hueco en la imagen de referencia (meta define posición final)
                Bitmap refConHueco = pintarBlancoEnReferencia();
                imagenReferencia.setImageBitmap(refConHueco);

                // 8) Copiar ese estado final a 'posiciones' y mezclar
                posiciones = new int[tamaño][tamaño];
                copiarMetaAposiciones();  // posiciones = meta (inicialmente)
                mezclarParaObtenerEstadoInicial(); // movimientos aleatorios

                // 9) Mostrar puzzle inicial en la UI
                actualizarUI();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 10) Iniciar cronometro
        iniciarCronometro();

        // 11) Resolver (no implementado)
        btnResolver.setOnClickListener(v -> {
            corriendo = false;
            handler.removeCallbacksAndMessages(null);
            Toast.makeText(getContext(), "Resolver automáticamente aún no implementado", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Divide la imagen escalada (300x300) en subBitmaps (nunca null)
     */
    private void dividirImagen(Bitmap bmpPuzzle) {
        int n = tamaño * tamaño;
        subBitmaps = new Bitmap[n];

        int anchoPieza = bmpPuzzle.getWidth() / tamaño;
        int altoPieza  = bmpPuzzle.getHeight() / tamaño;

        for (int fila = 0; fila < tamaño; fila++) {
            for (int col = 0; col < tamaño; col++) {
                int index = fila * tamaño + col;
                subBitmaps[index] = Bitmap.createBitmap(
                        bmpPuzzle,
                        col * anchoPieza,
                        fila * altoPieza,
                        anchoPieza,
                        altoPieza
                );
            }
        }
    }

    /**
     * Genera la matriz final meta[][] con 0..n-1 en orden,
     * y un hueco aleatorio
     */
    private void generarEstadoFinalConHuecoAleatorio() {
        int n = tamaño * tamaño;
        meta = new int[tamaño][tamaño];

        // Llenar meta con 0..(n-1)
        for (int i = 0; i < n; i++) {
            meta[i / tamaño][i % tamaño] = i;
        }

        // Elige hueco aleatorio
        Random rand = new Random();
        filaBlanca = rand.nextInt(tamaño);
        colBlanca  = rand.nextInt(tamaño);
        meta[filaBlanca][colBlanca] = -1;
    }

    /**
     * Pinta en la imagen original un parche donde está el hueco en meta
     */
    private Bitmap pintarBlancoEnReferencia() {
        // Crear copia mutable
        Bitmap mutableRef = bitmapOriginal.copy(Bitmap.Config.ARGB_8888, true);

        // Factor de escala: puzzle(300) vs original
        float ratioX = (float) originalWidth  / PUZZLE_SIZE;
        float ratioY = (float) originalHeight / PUZZLE_SIZE;

        // Determinar dónde está el hueco en meta
        // Buscamos filaBlanca,colBlanca
        // (ya definido en generarEstadoFinalConHuecoAleatorio)
        int anchoPieza = PUZZLE_SIZE / tamaño;
        int altoPieza  = PUZZLE_SIZE / tamaño;

        int x0Puzzle = colBlanca * anchoPieza;
        int y0Puzzle = filaBlanca * altoPieza;

        // Convertir coords puzzle -> coords original
        int x0Ref   = (int)(x0Puzzle  * ratioX);
        int y0Ref   = (int)(y0Puzzle  * ratioY);
        int anchoRef= (int)(anchoPieza* ratioX);
        int altoRef = (int)(altoPieza * ratioY);

        // Pintar en blanco
        for (int y = y0Ref; y < y0Ref + altoRef; y++) {
            for (int x = x0Ref; x < x0Ref + anchoRef; x++) {
                if (x>=0 && x<mutableRef.getWidth() &&
                        y>=0 && y<mutableRef.getHeight()) {
                    mutableRef.setPixel(x, y, Color.WHITE);
                }
            }
        }
        return mutableRef;
    }

    /**
     * Copia la matriz final meta[][] a posiciones[][]
     */
    private void copiarMetaAposiciones() {
        posiciones = new int[tamaño][tamaño];
        for (int fila = 0; fila < tamaño; fila++) {
            for (int col = 0; col < tamaño; col++) {
                posiciones[fila][col] = meta[fila][col];
            }
        }
    }

    /**
     * Realiza movimientos aleatorios en posiciones[][] para obtener el estado inicial
     * sin alterar meta[][]
     */
    private void mezclarParaObtenerEstadoInicial() {
        // Buscamos dónde está -1 en 'posiciones'
        int n = tamaño * tamaño;
        for (int i = 0; i < n; i++) {
            int f = i / tamaño;
            int c = i % tamaño;
            if (posiciones[f][c] == -1) {
                filaBlanca = f;
                colBlanca  = c;
                break;
            }
        }

        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
            int[] d = dirs[rand.nextInt(4)];
            int nf = filaBlanca + d[0];
            int nc = colBlanca + d[1];

            if (nf>=0 && nf<tamaño && nc>=0 && nc<tamaño) {
                int temp = posiciones[nf][nc];
                posiciones[nf][nc] = -1;
                posiciones[filaBlanca][colBlanca] = temp;

                filaBlanca = nf;
                colBlanca  = nc;
            }
        }
    }

    /**
     * Muestra el puzzle en la UI
     */
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
                params.width  = 0;
                params.height = 0;
                pieza.setLayoutParams(params);

                gridPuzzle.addView(pieza);
            }
        }
        gridPuzzle.invalidate();
        gridPuzzle.requestLayout();
    }

    /**
     * Cuando el usuario toca una pieza, si es adyacente al hueco, la mueve
     */
    private void moverPieza(View view) {
        int pos = gridPuzzle.indexOfChild(view);
        int filaAct = pos / tamaño;
        int colAct  = pos % tamaño;

        // Ver si es adyacente
        boolean esMovible =
                (filaAct == filaBlanca && Math.abs(colAct - colBlanca) == 1) ||
                        (colAct == colBlanca && Math.abs(filaAct - filaBlanca) == 1);

        if (esMovible) {
            int temp = posiciones[filaAct][colAct];
            posiciones[filaAct][colAct] = -1;
            posiciones[filaBlanca][colBlanca] = temp;

            filaBlanca = filaAct;
            colBlanca  = colAct;

            actualizarUI();
            verificarVictoria();
        }
    }

    /**
     * Verifica si 'posiciones' == 'meta'. Si sí, puzzle resuelto -> FinJuegoFragment
     */
    private void verificarVictoria() {
        boolean resuelto = true;
        for (int fila = 0; fila < tamaño; fila++) {
            for (int col = 0; col < tamaño; col++) {
                if (posiciones[fila][col] != meta[fila][col]) {
                    resuelto = false;
                    break;
                }
            }
            if (!resuelto) break;
        }

        if (resuelto) {
            corriendo = false;
            handler.removeCallbacksAndMessages(null);

            // Llamar a FinJuegoFragment
            // Obtenemos tiempo final
            String tiempoFinal = cronometro.getText().toString();
            // Por ejemplo, esAutomatico = false
            boolean esAutomatico = false;

            // Instanciar y mostrar
            FinJuegoFragment dialog = FinJuegoFragment.newInstance(tiempoFinal, esAutomatico);
            // O getChildFragmentManager()
            dialog.show(getParentFragmentManager(), "FinJuegoFragment");
        }
    }

    /**
     * Inicia el cronómetro en la parte superior
     */
    private void iniciarCronometro() {
        handler = new Handler();
        Runnable actualizarTiempo = new Runnable() {
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
        handler.post(actualizarTiempo);
    }
}
