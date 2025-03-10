package com.joshnisth.proyectorompecabezas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ArmarPuzzleActivity extends AppCompatActivity {

    private ImageView imagenReferencia;
    private GridLayout gridPuzzle;
    private TextView cronometro;
    private Handler handler;
    private int segundos = 0;
    private boolean corriendo = true;
    // Matriz de posiciones (3x3). -1 para la casilla vacía, 0..7 para piezas
    private int[][] posiciones;

    // Almacena los sub-bitmaps de la imagen en un array. Indices 0..7 y la posición 8 no se usa (porque es -1)
    private Bitmap[] subBitmaps = new Bitmap[9];

    private int filaBlanca = 2, colBlanca = 2; // Última posición en blanco
    private Button btnResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_armar_puzzle);

        btnResolver = findViewById(R.id.btnResolver);
        imagenReferencia = findViewById(R.id.imagenReferencia);
        gridPuzzle = findViewById(R.id.gridPuzzle);
        cronometro = findViewById(R.id.cronometro);

        // Obtener la imagen seleccionada de la actividad anterior
        Intent intent = getIntent();
        Uri imagenUri = Uri.parse(intent.getStringExtra("imagenUri"));

        try {
            InputStream imageStream = getContentResolver().openInputStream(imagenUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            imagenReferencia.setImageBitmap(bitmap);

            // Cortar la imagen en sub-bitmaps y guardar en subBitmaps[]
            cortarImagenEnBitmaps(bitmap);

            // Inicializar la matriz de posiciones y mezclar
            posiciones = new int[3][3];
            mostrarPiezasMezcladas();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Iniciar cronómetro
        iniciarCronometro();

        // Resolver automáticamente
        btnResolver.setOnClickListener(v -> resolverAutomaticamente());
    }

    /**
     * Corta el Bitmap en 9 sub-imágenes de igual tamaño y las guarda en subBitmaps[0..7].
     * subBitmaps[8] quedará en null, siendo la casilla vacía (-1).
     */
    private void cortarImagenEnBitmaps(Bitmap bitmap) {
        int ancho = bitmap.getWidth() / 3;
        int alto = bitmap.getHeight() / 3;

        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                int index = fila * 3 + col;
                // Si es la última casilla (2,2), no le asignamos bitmap (será la vacía)
                if (fila == 2 && col == 2) {
                    subBitmaps[index] = null;
                } else {
                    int x = col * ancho;
                    int y = fila * alto;
                    subBitmaps[index] = Bitmap.createBitmap(bitmap, x, y, ancho, alto);
                }
            }
        }
    }

    /**
     * Mezcla la matriz posiciones de forma que sea resoluble, luego llama a actualizarUI()
     */
    private void mostrarPiezasMezcladas() {
        // Armar la matriz en orden correcto y luego mezclar
        for (int i = 0; i < 9; i++) {
            posiciones[i / 3][i % 3] = (i == 8) ? -1 : i;
        }
        // El hueco está en (2,2) al inicio
        filaBlanca = 2;
        colBlanca = 2;

        // Mezclar con 100 movimientos aleatorios
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            int[][] direcciones = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            int[] dir = direcciones[rand.nextInt(4)];
            int nuevaFila = filaBlanca + dir[0];
            int nuevaCol = colBlanca + dir[1];

            if (nuevaFila >= 0 && nuevaFila < 3 && nuevaCol >= 0 && nuevaCol < 3) {
                // Intercambiar
                int temp = posiciones[filaBlanca][colBlanca];
                posiciones[filaBlanca][colBlanca] = posiciones[nuevaFila][nuevaCol];
                posiciones[nuevaFila][nuevaCol] = temp;

                filaBlanca = nuevaFila;
                colBlanca = nuevaCol;
            }
        }

        // Dibujar en la UI
        actualizarUI(posiciones);
    }

    /**
     * Actualiza la UI dibujando la matriz dada. Crea un nuevo ImageView por cada celda.
     * Si es -1, se dibuja un cuadro blanco; sino, se asigna el subBitmap correspondiente.
     */
    private void actualizarUI(int[][] estado) {
        gridPuzzle.removeAllViews();

        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                // Guardar el valor en nuestra matriz interna
                posiciones[fila][col] = estado[fila][col];

                ImageView pieza = new ImageView(this);
                if (estado[fila][col] == -1) {
                    pieza.setBackgroundColor(Color.WHITE);
                    filaBlanca = fila;
                    colBlanca = col;
                } else {
                    pieza.setBackgroundColor(Color.TRANSPARENT);
                    pieza.setImageBitmap(subBitmaps[ estado[fila][col] ]);
                    pieza.setOnClickListener(this::moverPieza);
                }

                // Ajustar LayoutParams para que se repartan equitativamente
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(fila, 1f),
                        GridLayout.spec(col, 1f)
                );
                params.width = 0;
                params.height = 0;
                pieza.setLayoutParams(params);

                gridPuzzle.addView(pieza);
            }
        }
        gridPuzzle.invalidate();
        gridPuzzle.requestLayout();
    }

    /**
     * Método para mover una ficha visualmente
     */
    private void moverPieza(View view) {
        // Hallar la posición en la cuadrícula
        int pos = gridPuzzle.indexOfChild(view);
        int filaActual = pos / 3;
        int colActual = pos % 3;

        // Verificar si es adyacente al hueco
        if ((filaActual == filaBlanca && Math.abs(colActual - colBlanca) == 1) ||
                (colActual == colBlanca && Math.abs(filaActual - filaBlanca) == 1)) {

            // Intercambiar en la matriz
            int temp = posiciones[filaActual][colActual];
            posiciones[filaActual][colActual] = posiciones[filaBlanca][colBlanca];
            posiciones[filaBlanca][colBlanca] = temp;

            // Actualizar hueco
            filaBlanca = filaActual;
            colBlanca = colActual;

            // Redibujar
            actualizarUI(posiciones);
            verificarVictoria();
        }
    }

    /**
     * Verifica si la matriz posiciones ya está en el estado resuelto:
     *  0 1 2
     *  3 4 5
     *  6 7 -1
     */
    private void verificarVictoria() {
        boolean resuelto = true;
        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                int esperado = fila * 3 + col;
                if (fila == 2 && col == 2) {
                    // Debe ser -1
                    if (posiciones[fila][col] != -1) {
                        resuelto = false;
                        break;
                    }
                } else {
                    if (posiciones[fila][col] != esperado) {
                        resuelto = false;
                        break;
                    }
                }
            }
            if (!resuelto) break;
        }

        if (resuelto) {
            corriendo = false;
            handler.removeCallbacksAndMessages(null); // Detener cronómetro
            Toast.makeText(this, "¡Puzzle Completado!", Toast.LENGTH_SHORT).show();
            btnResolver.setEnabled(false);
            btnResolver.setText("Resuelto");
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

    /**
     * Lógica para resolver automáticamente con A*. Devuelve secuencia de estados (matrices)
     * hasta llegar a la meta, y los va dibujando uno a uno.
     */
    private void resolverAutomaticamente() {
        corriendo = false;
        handler.removeCallbacksAndMessages(null);
        new Thread(() -> {
            PuzzleSolver solver = new PuzzleSolver();
            List<int[][]> solucion = solver.solve(posiciones);

            if (solucion == null) {
                runOnUiThread(() ->
                        Toast.makeText(ArmarPuzzleActivity.this, "No se encontró solución", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // Reproducir los pasos de la solución con un retardo
            for (int stepIndex = 0; stepIndex < solucion.size(); stepIndex++) {
                int[][] estado = solucion.get(stepIndex);

                // Log para ver la matriz generada por el solver
                Log.d("SOLVER_STEPS", "Paso " + stepIndex);
                imprimirMatrizEnLog(estado, "SOLVER_STEPS");

                runOnUiThread(() -> {
                    actualizarUI(estado);

                });

                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Al terminar, mostrar mensaje
            runOnUiThread(() -> {

                Toast.makeText(ArmarPuzzleActivity.this, "¡Puzzle resuelto automáticamente!", Toast.LENGTH_SHORT).show();
                btnResolver.setEnabled(false);
                btnResolver.setText("Resuelto");
            });
        }).start();
    }

    /**
     * Función auxiliar para imprimir una matriz 3x3 pasada como parámetro
     */
    private void imprimirMatrizEnLog(int[][] matriz, String tag) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(Arrays.toString(matriz[i]));
            sb.append("\n");
        }
        Log.d(tag, "\n" + sb.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        corriendo = false; // Detener el cronómetro si la actividad se destruye
    }
}
