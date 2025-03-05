package com.joshnisth.proyectorompecabezas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArmarPuzzleActivity extends AppCompatActivity {

    private ImageView imagenReferencia;
    private GridLayout gridPuzzle;
    private TextView cronometro;
    private Handler handler;
    private int segundos = 0;
    private boolean corriendo = true;
    private List<ImageView> piezasPuzzle;
    private int[][] posiciones;
    private int filaBlanca = 2, colBlanca = 2; // Última posición en blanco

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_armar_puzzle);

        imagenReferencia = findViewById(R.id.imagenReferencia);
        gridPuzzle = findViewById(R.id.gridPuzzle);
        cronometro = findViewById(R.id.cronometro);
        Button btnResolver = findViewById(R.id.btnResolver);

        // Obtener la imagen seleccionada de la actividad anterior
        Intent intent = getIntent();
        Uri imagenUri = Uri.parse(intent.getStringExtra("imagenUri"));

        try {
            InputStream imageStream = getContentResolver().openInputStream(imagenUri);
            Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
            imagenReferencia.setImageBitmap(bitmap);

            // Cortar la imagen en 9 piezas
            piezasPuzzle = cortarImagen(bitmap);
            mostrarPiezasMezcladas();

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Iniciar cronómetro
        iniciarCronometro();

        // Resolver automáticamente (A* pendiente)
        btnResolver.setOnClickListener(v -> resolverAutomaticamente());
    }

    // Método para cortar la imagen en 9 partes
    private List<ImageView> cortarImagen(Bitmap bitmap) {
        // Dividimos la imagen en 3 columnas y 3 filas
        int ancho = bitmap.getWidth() / 3;
        int alto = bitmap.getHeight() / 3;

        List<ImageView> piezas = new ArrayList<>();
        posiciones = new int[3][3];

        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                // Crear un ImageView para esta pieza
                ImageView piezaView = new ImageView(this);
                piezaView.setPadding(2, 2, 2, 2);

                // Creamos los LayoutParams para que cada pieza llene su celda en el GridLayout
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(fila, 1f), // rowSpec
                        GridLayout.spec(col, 1f)   // columnSpec
                );
                // Asignamos ancho y alto 0 para que se repartan con "pesos"
                params.width = 0;
                params.height = 0;
                piezaView.setLayoutParams(params);

                // Si es la última posición (2,2), la dejamos en blanco
                if (fila == 2 && col == 2) {
                    piezaView.setBackgroundColor(Color.WHITE);
                    posiciones[fila][col] = -1;
                } else {
                    // Cortar el pedazo correspondiente del bitmap original
                    int x = col * ancho;
                    int y = fila * alto;
                    Bitmap subImagen = Bitmap.createBitmap(bitmap, x, y, ancho, alto);
                    piezaView.setImageBitmap(subImagen);
                    posiciones[fila][col] = fila * 3 + col;
                }

                // Guardar la pieza en la lista y setear OnClick
                piezaView.setOnClickListener(this::moverPieza);
                piezas.add(piezaView);
            }
        }
        return piezas;
    }


    // Mostrar las piezas mezcladas en el GridLayout de manera resoluble
    private void mostrarPiezasMezcladas() {
        gridPuzzle.removeAllViews(); // Eliminar vistas antiguas

        mezclarPiezasResolubles(); // Mezclar la matriz

        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                int indicePieza = posiciones[fila][col];
                ImageView pieza;

                if (indicePieza == -1) {
                    // Crear una vista en blanco
                    pieza = new ImageView(this);
                    pieza.setBackgroundColor(Color.WHITE);
                    filaBlanca = fila;
                    colBlanca = col;
                } else {
                    pieza = piezasPuzzle.get(indicePieza);
                }

                // Ajustar parámetros y añadir la pieza a la cuadrícula
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
    }

    // Método para asegurar que el rompecabezas sea resoluble
    private void mezclarPiezasResolubles() {
        Log.d("MEZCLA", "Antes de mezclar:");
        imprimirMatriz();

        for (int i = 0; i < 9; i++) {
            posiciones[i / 3][i % 3] = (i == 8) ? -1 : i;
        }

        int movimientos = 100;
        Random rand = new Random();

        for (int i = 0; i < movimientos; i++) {
            int[][] direcciones = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            int[] direccionElegida = direcciones[rand.nextInt(4)];
            int nuevaFila = filaBlanca + direccionElegida[0];
            int nuevaColumna = colBlanca + direccionElegida[1];

            if (nuevaFila >= 0 && nuevaFila < 3 && nuevaColumna >= 0 && nuevaColumna < 3) {
                posiciones[filaBlanca][colBlanca] = posiciones[nuevaFila][nuevaColumna];
                posiciones[nuevaFila][nuevaColumna] = -1;

                filaBlanca = nuevaFila;
                colBlanca = nuevaColumna;
            }
        }

        Log.d("MEZCLA", "Después de mezclar:");
        imprimirMatriz();
    }

    private void imprimirMatriz() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(posiciones[i][j]).append(" ");
            }
            sb.append("\n");
        }
        Log.d("MATRIZ", "\n" + sb.toString());
    }

    // Método para mover las piezas del rompecabezas
    private void moverPieza(View view) {
        // Obtener la posición actual de la pieza seleccionada
        int posicionActual = gridPuzzle.indexOfChild(view);
        int filaActual = posicionActual / 3;
        int colActual = posicionActual % 3;

        // Verificar si la pieza es adyacente a la pieza vacía
        boolean esMovible = (filaActual == filaBlanca && Math.abs(colActual - colBlanca) == 1) ||
                (colActual == colBlanca && Math.abs(filaActual - filaBlanca) == 1);

        if (esMovible) {
            // Obtener las vistas de la pieza seleccionada y la pieza vacía
            ImageView piezaSeleccionada = (ImageView) view;
            ImageView piezaBlancaView = (ImageView) gridPuzzle.getChildAt(filaBlanca * 3 + colBlanca);

            // Intercambiar imágenes
            Drawable tempImagen = piezaSeleccionada.getDrawable();
            piezaSeleccionada.setImageDrawable(null);
            piezaSeleccionada.setBackgroundColor(Color.WHITE);
            // Quitar el listener de la vista que ahora es blanca
            piezaSeleccionada.setOnClickListener(null);

            piezaBlancaView.setImageDrawable(tempImagen);
            // Asignar el listener a la vista que ahora tiene la imagen para que pueda moverse en futuros toques
            piezaBlancaView.setOnClickListener(this::moverPieza);

            // Intercambiar valores en la matriz de posiciones
            int tempValor = posiciones[filaActual][colActual];
            posiciones[filaActual][colActual] = posiciones[filaBlanca][colBlanca];
            posiciones[filaBlanca][colBlanca] = tempValor;

            // Actualizar la posición de la pieza vacía
            filaBlanca = filaActual;
            colBlanca = colActual;

            gridPuzzle.invalidate();
            gridPuzzle.requestLayout();

            // Verificar si el puzzle está resuelto
            verificarVictoria();
        }
    }




    // Método para verificar si el puzzle está resuelto
    // Método mejorado para verificar si el puzzle está resuelto
    private void verificarVictoria() {
        boolean resuelto = true;

        // La verificación debe ser ajustada para el orden correcto
        // En un rompecabezas 3x3 normal, el orden final debería ser:
        // 0 1 2
        // 3 4 5
        // 6 7 -1 (espacio vacío)

        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                int posEsperada = fila * 3 + col;

                // La última posición (2,2) debe ser -1 (espacio vacío)
                if (fila == 2 && col == 2) {
                    if (posiciones[fila][col] != -1) {
                        resuelto = false;
                        break;
                    }
                }
                // Para todas las demás posiciones, verifica el orden correcto
                else if (posiciones[fila][col] != posEsperada) {
                    resuelto = false;
                    break;
                }
            }
            if (!resuelto) break;
        }

        if (resuelto) {
            corriendo = false;
            Toast.makeText(this, "¡Puzzle Completado!", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para iniciar el cronómetro
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

    // Método para resolver automáticamente (A* pendiente)
    private void resolverAutomaticamente() {
        int[] estadoInicial = new int[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                estadoInicial[i * 3 + j] = posiciones[i][j];
            }
        }

        new Thread(() -> {
            PuzzleSolver solver = new PuzzleSolver();
            List<int[]> solucion = solver.solve(estadoInicial);
            if (solucion == null) {
                runOnUiThread(() ->
                        Toast.makeText(ArmarPuzzleActivity.this, "No se encontró solución", Toast.LENGTH_SHORT).show()
                );
                return;
            }
            for (int[] estado : solucion) {
                runOnUiThread(() -> actualizarUIConEstado(estado));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() ->
                    Toast.makeText(ArmarPuzzleActivity.this, "¡Puzzle resuelto automáticamente!", Toast.LENGTH_SHORT).show()
            );
        }).start();
    }
    private void actualizarUIConEstado(int[] estado) {
        gridPuzzle.removeAllViews(); // Eliminar las vistas actuales

        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                int indice = fila * 3 + col;
                ImageView pieza;

                if (estado[indice] == -1) {
                    // Crear una vista en blanco
                    pieza = new ImageView(this);
                    pieza.setBackgroundColor(Color.WHITE);
                    filaBlanca = fila;
                    colBlanca = col;
                } else {
                    pieza = piezasPuzzle.get(estado[indice]);
                }

                // Ajustar los parámetros y añadir la pieza a la cuadrícula
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

        // Forzar actualización de la UI
        gridPuzzle.invalidate();
        gridPuzzle.requestLayout();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        corriendo = false;
    }
}
