package com.joshnisth.proyectorompecabezas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;
import com.joshnisth.proyectorompecabezas.data.models.Rompecabezas;
import com.joshnisth.proyectorompecabezas.data.repositories.PuntuacionRepository;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configuración del botón personalizado
        ImageButton btnPersonalizado = findViewById(R.id.btnPersonalizado);
        btnPersonalizado.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CrearPuzzleActivity.class);
            startActivity(intent);
        });

        // TEST ROOM DATABASE
        PuntuacionRepository repo = new PuntuacionRepository(this);

        // Insertar datos de prueba
        repo.insertarJugador(new Jugador("Josh", 120));
        repo.insertarRompecabezas(new Rompecabezas("Puzzle Bolivia",
                "/data/user/0/com.joshnisth.proyectorompecabezas/files/imagenes_puzzle/bolivia.png"));

        // Recuperar datos y mostrarlos en Log
        new Thread(() -> {
            List<Jugador> jugadores = repo.obtenerMejoresTiempos();
            for (Jugador j : jugadores) {
                Log.d("ROOM_TEST", "Jugador: " + j.getNombre() + " - Tiempo: " + j.getTiempo());
            }

            List<Rompecabezas> puzzles = repo.obtenerRompecabezas();
            for (Rompecabezas p : puzzles) {
                Log.d("ROOM_TEST", "Rompecabezas: " + p.getNombre() + " - Ruta: " + p.getRutaImagen());
            }
        }).start();
    }
}
