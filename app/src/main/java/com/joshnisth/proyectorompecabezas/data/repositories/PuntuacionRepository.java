package com.joshnisth.proyectorompecabezas.data.repositories;

import android.content.Context;
import com.joshnisth.proyectorompecabezas.data.database.AppDatabase;
import com.joshnisth.proyectorompecabezas.data.models.Jugador;
import com.joshnisth.proyectorompecabezas.data.models.Rompecabezas;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PuntuacionRepository {
    private AppDatabase db;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PuntuacionRepository(Context context) {
        db = AppDatabase.getInstance(context);
    }

    // Métodos para Jugador
    public void insertarJugador(Jugador jugador) {
        executorService.execute(() -> db.jugadorDao().insertar(jugador));
    }

    public List<Jugador> obtenerMejoresTiempos() {
        return db.jugadorDao().obtenerMejoresTiempos();
    }

    // Métodos para Rompecabezas
    public void insertarRompecabezas(Rompecabezas rompecabezas) {
        executorService.execute(() -> db.rompecabezasDao().insertar(rompecabezas));
    }

    public List<Rompecabezas> obtenerRompecabezas() {
        return db.rompecabezasDao().obtenerTodos();
    }
}
