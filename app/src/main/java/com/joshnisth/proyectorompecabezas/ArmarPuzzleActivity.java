package com.joshnisth.proyectorompecabezas;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
        int ancho = 100; // Cada pieza debe medir 100x100 dp
        int alto = 100;
        List<ImageView> piezas = new ArrayList<>();
        posiciones = new int[3][3];

        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                ImageView piezaView = new ImageView(this);
                piezaView.setLayoutParams(new GridLayout.LayoutParams());
                piezaView.setPadding(2, 2, 2, 2); // Márgenes entre piezas

                if (fila == 2 && col == 2) {
                    piezaView.setBackgroundColor(Color.WHITE);
                    posiciones[fila][col] = -1;
                } else {
                    int x = col * ancho;
                    int y = fila * alto;
                    Bitmap subImagen = Bitmap.createBitmap(bitmap, x, y, ancho, alto);
                    piezaView.setImageBitmap(subImagen);
                    posiciones[fila][col] = piezas.size();
                }

                piezaView.setOnClickListener(this::moverPieza);
                piezas.add(piezaView);
            }
        }
        return piezas;
    }

    // Mostrar las piezas mezcladas en el GridLayout de manera resoluble
    private void mostrarPiezasMezcladas() {
        gridPuzzle.removeAllViews();

        // Mezclar las piezas (asegurando que sea resoluble)
        mezclarPiezasResolubles();

        // Actualizar la matriz de posiciones y añadir las piezas al grid
        for (int fila = 0; fila < 3; fila++) {
            for (int col = 0; col < 3; col++) {
                int pos = fila * 3 + col;
                int indicePieza = posiciones[fila][col];

                if (indicePieza == -1) {
                    // Actualizar la posición de la pieza en blanco
                    filaBlanca = fila;
                    colBlanca = col;
                }

                ImageView pieza = piezasPuzzle.get(indicePieza >= 0 ? indicePieza : piezasPuzzle.size() - 1);
                gridPuzzle.addView(pieza);
            }
        }
    }

    // Método para asegurar que el rompecabezas sea resoluble
    private void mezclarPiezasResolubles() {
        // Reiniciar la matriz de posiciones a su estado original
        for (int i = 0; i < 9; i++) {
            posiciones[i / 3][i % 3] = (i == 8) ? -1 : i;
        }

        // Realizar un número aleatorio de movimientos válidos (100-200)
        int movimientos = 100 + (int)(Math.random() * 100);

        for (int i = 0; i < movimientos; i++) {
            // Elegir una dirección aleatoria (0: arriba, 1: derecha, 2: abajo, 3: izquierda)
            int direccion = (int)(Math.random() * 4);

            // Calcular nueva posición según la dirección
            int nuevaFila = filaBlanca;
            int nuevaCol = colBlanca;

            switch (direccion) {
                case 0: // Arriba
                    nuevaFila = Math.max(0, filaBlanca - 1);
                    break;
                case 1: // Derecha
                    nuevaCol = Math.min(2, colBlanca + 1);
                    break;
                case 2: // Abajo
                    nuevaFila = Math.min(2, filaBlanca + 1);
                    break;
                case 3: // Izquierda
                    nuevaCol = Math.max(0, colBlanca - 1);
                    break;
            }

            // Si la posición cambió, realizar un intercambio válido
            if (nuevaFila != filaBlanca || nuevaCol != colBlanca) {
                // Intercambiar valores en la matriz de posiciones
                int temp = posiciones[nuevaFila][nuevaCol];
                posiciones[nuevaFila][nuevaCol] = -1;
                posiciones[filaBlanca][colBlanca] = temp;

                // Actualizar posición en blanco
                filaBlanca = nuevaFila;
                colBlanca = nuevaCol;
            }
        }
    }

    // Método para mover las piezas del rompecabezas
    private void moverPieza(View view) {
        // Encontrar la posición actual de la pieza seleccionada en el grid
        int posicionActual = gridPuzzle.indexOfChild(view);
        int filaActual = posicionActual / 3;
        int colActual = posicionActual % 3;

        // Calcular la posición de la pieza en blanco
        int posicionBlanca = filaBlanca * 3 + colBlanca;

        // Verificar si la pieza seleccionada es adyacente a la posición en blanco
        if ((filaActual == filaBlanca && Math.abs(colActual - colBlanca) == 1) ||
                (colActual == colBlanca && Math.abs(filaActual - filaBlanca) == 1)) {

            // Obtener las vistas de la pieza seleccionada y la pieza en blanco
            View piezaSeleccionada = gridPuzzle.getChildAt(posicionActual);
            View piezaBlanca = gridPuzzle.getChildAt(posicionBlanca);

            // Eliminar ambas piezas del grid
            gridPuzzle.removeView(piezaSeleccionada);
            gridPuzzle.removeView(piezaBlanca);

            // Volver a añadir las piezas en las posiciones intercambiadas
            // (Importante: el orden de añadir es crucial si las posiciones son consecutivas)
            if (posicionActual < posicionBlanca) {
                gridPuzzle.addView(piezaBlanca, posicionActual);
                gridPuzzle.addView(piezaSeleccionada, posicionBlanca);
            } else {
                gridPuzzle.addView(piezaSeleccionada, posicionBlanca);
                gridPuzzle.addView(piezaBlanca, posicionActual);
            }

            // Actualizar la matriz de posiciones para el seguimiento lógico
            int valorTemporal = posiciones[filaActual][colActual];
            posiciones[filaActual][colActual] = posiciones[filaBlanca][colBlanca]; // -1
            posiciones[filaBlanca][colBlanca] = valorTemporal;

            // Actualizar la posición en blanco
            filaBlanca = filaActual;
            colBlanca = colActual;

            // Verificar si el puzzle se ha completado
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
        // Implementar el algoritmo A*
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        corriendo = false;
    }
}
