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

    // ✅ **Guardar jugador con su tamaño de puzzle**
    public void insertarJugador(Jugador jugador) {
        executorService.execute(() -> db.jugadorDao().insertar(jugador));
    }


    // ✅ **Obtener ranking filtrado por tamaño (3x3 o 4x4)**
    public List<Jugador> obtenerMejoresTiemposPorTamano(int tamano) {
        return db.jugadorDao().obtenerMejoresTiemposPorTamano(tamano);
    }

    // ✅ **Guardar un nuevo rompecabezas personalizado**
    public void insertarRompecabezas(Rompecabezas rompecabezas) {
        executorService.execute(() -> db.rompecabezasDao().insertar(rompecabezas));
    }

    // ✅ **Obtener todos los rompecabezas guardados**
    public List<Rompecabezas> obtenerRompecabezas() {
        return db.rompecabezasDao().obtenerTodos();
    }
}
